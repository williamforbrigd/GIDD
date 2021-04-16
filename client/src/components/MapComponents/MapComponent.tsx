import Button from '@material-ui/core/Button';
import React, { useState, useEffect } from 'react';
import { GoogleMap, withGoogleMap } from 'react-google-maps';
import Activities from '../../containers/Activities';

interface DefaultCenter {
    lat: number;
    lng: number;
}

const Map = withGoogleMap<{ defaultCenter: DefaultCenter }>((props) => (
    <GoogleMap defaultZoom={15} defaultCenter={props.defaultCenter}>
        {props.children}
    </GoogleMap>
));

interface Props {
    defaultCenter:DefaultCenter; 
    children?:React.ReactChild; 
}
const MapComponent = ({defaultCenter, children}: Props) => {

    const WrappedMap = Map;
    return (
        <div style={{ width: '100vw', height: '90vh' }}>
            <WrappedMap
                defaultCenter={defaultCenter}
                containerElement={<div style={{ height: '100%' }} />}
                mapElement={<div style={{ height: '100%' }} />}
            >
                {children}
            </WrappedMap>
        </div>
    );
};

export default MapComponent;