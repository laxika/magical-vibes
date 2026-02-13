package com.github.laxika.magicalvibes.networking;

import com.github.laxika.magicalvibes.networking.message.BottomCardsRequest;
import com.github.laxika.magicalvibes.networking.message.CardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.CreateGameRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.JoinGameRequest;
import com.github.laxika.magicalvibes.networking.message.KeepHandRequest;
import com.github.laxika.magicalvibes.networking.message.LoginRequest;
import com.github.laxika.magicalvibes.networking.message.MulliganRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.networking.message.SetAutoStopsRequest;
import com.github.laxika.magicalvibes.networking.message.ActivateAbilityRequest;
import com.github.laxika.magicalvibes.networking.message.ColorChosenRequest;
import com.github.laxika.magicalvibes.networking.message.MayAbilityChosenRequest;
import com.github.laxika.magicalvibes.networking.message.PermanentChosenRequest;
import com.github.laxika.magicalvibes.networking.message.SacrificePermanentRequest;
import com.github.laxika.magicalvibes.networking.message.TapPermanentRequest;

public interface MessageHandler {

    void handleLogin(Connection connection, LoginRequest request) throws Exception;

    void handleCreateGame(Connection connection, CreateGameRequest request) throws Exception;

    void handleJoinGame(Connection connection, JoinGameRequest request) throws Exception;

    void handlePassPriority(Connection connection, PassPriorityRequest request) throws Exception;

    void handleKeepHand(Connection connection, KeepHandRequest request) throws Exception;

    void handleMulligan(Connection connection, MulliganRequest request) throws Exception;

    void handleBottomCards(Connection connection, BottomCardsRequest request) throws Exception;

    void handlePlayCard(Connection connection, PlayCardRequest request) throws Exception;

    void handleTapPermanent(Connection connection, TapPermanentRequest request) throws Exception;

    void handleSacrificePermanent(Connection connection, SacrificePermanentRequest request) throws Exception;

    void handleActivateAbility(Connection connection, ActivateAbilityRequest request) throws Exception;

    void handleSetAutoStops(Connection connection, SetAutoStopsRequest request) throws Exception;

    void handleDeclareAttackers(Connection connection, DeclareAttackersRequest request) throws Exception;

    void handleDeclareBlockers(Connection connection, DeclareBlockersRequest request) throws Exception;

    void handleCardChosen(Connection connection, CardChosenRequest request) throws Exception;

    void handlePermanentChosen(Connection connection, PermanentChosenRequest request) throws Exception;

    void handleColorChosen(Connection connection, ColorChosenRequest request) throws Exception;

    void handleMayAbilityChosen(Connection connection, MayAbilityChosenRequest request) throws Exception;

    void handleTimeout(Connection connection);

    void handleError(Connection connection, String message) throws Exception;
}
