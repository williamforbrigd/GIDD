import React, { useState, ChangeEvent, KeyboardEventHandler } from 'react';
import { TextField, Button } from '@material-ui/core';
import styled from 'styled-components';
import Popup from '../components/Popup';
import ActivityForm from '../components/ActivityForm';
import MyUser from '../components/MyUser';

const Container = styled.div`
    display: flex;
    justify-content: center;
    margin-top: 100px;
`;

const HomePage = () => {
    const [openPopup, setOpenPopup] = useState<boolean>(false);
    const [openUser, setOpenUser] = useState<boolean>(false);

    return (
        <Container>
            <h1>Dette er hjemmesiden</h1>
            <button onClick={() => setOpenPopup(!openPopup)}>Popup</button>
            <Popup
                title="Legg til aktivitet"
                openPopup={openPopup}
                setOpenPopup={setOpenPopup}
            >
                <ActivityForm
                    openPopup={openPopup}
                    setOpenPopup={setOpenPopup}
                />
            </Popup>
            <button onClick={() => setOpenUser(!openUser)}>Min Side</button>
            <Popup
                title="Min Bruker"
                openPopup={openUser}
                setOpenPopup={setOpenUser}
            >
                <MyUser />
            </Popup>
        </Container>
    );
};

export default HomePage;
