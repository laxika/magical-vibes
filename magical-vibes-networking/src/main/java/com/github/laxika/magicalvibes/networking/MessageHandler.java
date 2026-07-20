package com.github.laxika.magicalvibes.networking;

import com.github.laxika.magicalvibes.networking.message.BottomCardsRequest;
import com.github.laxika.magicalvibes.networking.message.CreateGameRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.JoinGameRequest;
import com.github.laxika.magicalvibes.networking.message.KeepHandRequest;
import com.github.laxika.magicalvibes.networking.message.LoginRequest;
import com.github.laxika.magicalvibes.networking.message.MulliganRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.networking.message.RegisterRequest;
import com.github.laxika.magicalvibes.networking.message.SetAutoStopsRequest;
import com.github.laxika.magicalvibes.networking.message.ActivateAbilityRequest;
import com.github.laxika.magicalvibes.networking.message.ActivateGraveyardAbilityRequest;
import com.github.laxika.magicalvibes.networking.message.ActivateHandAbilityRequest;
import com.github.laxika.magicalvibes.networking.message.SacrificePermanentRequest;
import com.github.laxika.magicalvibes.networking.message.TapPermanentRequest;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignedRequest;
import com.github.laxika.magicalvibes.networking.message.InteractionAnswerRequest;
import com.github.laxika.magicalvibes.networking.message.CreateDraftRequest;
import com.github.laxika.magicalvibes.networking.message.DraftPickRequest;
import com.github.laxika.magicalvibes.networking.message.RequestCardListRequest;
import com.github.laxika.magicalvibes.networking.message.SubmitDeckRequest;
import com.github.laxika.magicalvibes.networking.message.PaySearchTaxRequest;
import com.github.laxika.magicalvibes.networking.message.RevertManaActivationsRequest;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsRequest;
import com.github.laxika.magicalvibes.networking.message.SaveDeckRequest;

import java.util.UUID;

public interface MessageHandler {

    void handleLogin(Connection connection, LoginRequest request) throws Exception;

    void handleRegister(Connection connection, RegisterRequest request) throws Exception;

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

    void handleActivateGraveyardAbility(Connection connection, ActivateGraveyardAbilityRequest request) throws Exception;

    void handleActivateHandAbility(Connection connection, ActivateHandAbilityRequest request) throws Exception;

    void handleSetAutoStops(Connection connection, SetAutoStopsRequest request) throws Exception;

    void handleDeclareAttackers(Connection connection, DeclareAttackersRequest request) throws Exception;

    void handleDeclareBlockers(Connection connection, DeclareBlockersRequest request) throws Exception;

    void handleInteractionAnswer(Connection connection, InteractionAnswerRequest request) throws Exception;

    void handleCreateDraft(Connection connection, CreateDraftRequest request) throws Exception;

    void handleDraftPick(Connection connection, DraftPickRequest request) throws Exception;

    void handleSubmitDeck(Connection connection, SubmitDeckRequest request) throws Exception;

    void handleCombatDamageAssigned(Connection connection, CombatDamageAssignedRequest request) throws Exception;

    void handleRequestCardList(Connection connection, RequestCardListRequest request) throws Exception;

    void handleValidTargetsRequest(Connection connection, ValidTargetsRequest request) throws Exception;

    void handlePaySearchTax(Connection connection, PaySearchTaxRequest request) throws Exception;

    void handleRevertManaActivations(Connection connection, RevertManaActivationsRequest request) throws Exception;

    void handleSaveDeck(Connection connection, SaveDeckRequest request) throws Exception;

    void handleSurrender(Connection connection) throws Exception;

    void handleLeaveGame(Connection connection) throws Exception;

    void handleLeaveDraft(Connection connection) throws Exception;

    void handleTimeout(Connection connection);

    void handleError(Connection connection, String message) throws Exception;

    void handleConnectionClosed(UUID playerId);
}
