import React from 'react';
import {
    Dialog,
    DialogTitle,
    DialogContent,
    makeStyles,
    Typography,
    Button,
} from '@material-ui/core';

interface Props {
    title?: string;
    children?: React.ReactNode;
    openPopup: boolean;
    setOpenPopup: React.Dispatch<React.SetStateAction<boolean>>;
}

const useStyles = makeStyles((theme) => ({
    dialogWrapper: {
        padding: theme.spacing(2),
        position: 'absolute',
        justifyContent: 'center',
        top: theme.spacing(5),
        maxHeight: 'auto',
        marginTop: '-20px'
    },
    dialogTitle: {
        paddingRight: '0px',
    },
}));

const Popup = ({ title, openPopup, setOpenPopup, children }: Props) => {
    const classes = useStyles();
    return (
        <Dialog
            open={openPopup}
            maxWidth="md"
            classes={{ paper: classes.dialogWrapper }}
        >
            <DialogTitle className={classes.dialogTitle}>
                <div style={{ display: 'flex' }}>
                    {/* The typography and button component will be shown in a single line*/}
                    <Typography
                        variant="h6"
                        component="div"
                        style={{ flexGrow: 1 }}
                    >
                        {title}
                    </Typography>
                    <Button
                        color="secondary"
                        onClick={() => {
                            setOpenPopup(!openPopup);
                        }}
                    >
                        X
                    </Button>
                </div>
            </DialogTitle>
            <DialogContent dividers>{children}</DialogContent>
        </Dialog>
    );
};

export default Popup;