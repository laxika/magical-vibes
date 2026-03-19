package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromGraveyardMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseFromListMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseFromRevealedHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsFromGraveyardsMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultiplePermanentsMessage;
import com.github.laxika.magicalvibes.networking.message.ChoosePermanentMessage;
import com.github.laxika.magicalvibes.networking.message.MayAbilityMessage;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.networking.message.XValueChoiceMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerInputServiceTest {

    @Mock private SessionManager sessionManager;
    @Mock private CardViewFactory cardViewFactory;

    @InjectMocks
    private PlayerInputService svc;

    @Captor private ArgumentCaptor<Object> messageCaptor;

    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Player1");
        gd.playerIdToName.put(PLAYER2_ID, "Player2");
        gd.playerBattlefields.put(PLAYER1_ID, new ArrayList<>());
        gd.playerBattlefields.put(PLAYER2_ID, new ArrayList<>());
        gd.playerGraveyards.put(PLAYER1_ID, new ArrayList<>());
        gd.playerGraveyards.put(PLAYER2_ID, new ArrayList<>());
        gd.playerExiledCards.put(PLAYER1_ID, new ArrayList<>());
        gd.playerExiledCards.put(PLAYER2_ID, new ArrayList<>());
        gd.playerDecks.put(PLAYER1_ID, new ArrayList<>());
        gd.playerDecks.put(PLAYER2_ID, new ArrayList<>());
        gd.playerHands.put(PLAYER1_ID, new ArrayList<>());
        gd.playerHands.put(PLAYER2_ID, new ArrayList<>());
        gd.playerManaPools.put(PLAYER1_ID, new ManaPool());
        gd.playerManaPools.put(PLAYER2_ID, new ManaPool());
    }

    private Card createCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    private Card createCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    // ========================================================================
    // Mind control redirection
    // ========================================================================

    @Nested
    @DisplayName("Mind control message redirection")
    class MindControlRedirection {

        @Test
        @DisplayName("Sends to controller when player is mind-controlled")
        void sendsToControllerWhenMindControlled() {
            UUID controllerId = UUID.randomUUID();
            gd.playerIdToName.put(controllerId, "Controller");
            gd.mindControlledPlayerId = PLAYER1_ID;
            gd.mindControllerPlayerId = controllerId;

            svc.beginCardChoice(gd, PLAYER1_ID, List.of(0, 1), "Pick a card");

            verify(sessionManager).sendToPlayer(eq(controllerId), any(ChooseCardFromHandMessage.class));
        }

        @Test
        @DisplayName("Sends to player directly when not mind-controlled")
        void sendsDirectlyWhenNotMindControlled() {
            svc.beginCardChoice(gd, PLAYER1_ID, List.of(0), "Pick a card");

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), any(ChooseCardFromHandMessage.class));
        }

        @Test
        @DisplayName("Sends to player directly when different player is mind-controlled")
        void sendsDirectlyWhenOtherPlayerMindControlled() {
            gd.mindControlledPlayerId = PLAYER2_ID;
            gd.mindControllerPlayerId = UUID.randomUUID();

            svc.beginCardChoice(gd, PLAYER1_ID, List.of(0), "Pick a card");

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), any(ChooseCardFromHandMessage.class));
        }
    }

    // ========================================================================
    // beginCardChoice
    // ========================================================================

    @Nested
    @DisplayName("beginCardChoice")
    class BeginCardChoice {

        @Test
        @DisplayName("Sets interaction state to CARD_CHOICE")
        void setsInteractionState() {
            svc.beginCardChoice(gd, PLAYER1_ID, List.of(0, 1, 2), "Choose a card");

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.CARD_CHOICE);
        }

        @Test
        @DisplayName("Sends correct message with valid indices and prompt")
        void sendsCorrectMessage() {
            List<Integer> indices = List.of(0, 2, 4);

            svc.beginCardChoice(gd, PLAYER1_ID, indices, "Choose one");

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseCardFromHandMessage msg = (ChooseCardFromHandMessage) messageCaptor.getValue();
            assertThat(msg.cardIndices()).containsExactly(0, 2, 4);
            assertThat(msg.prompt()).isEqualTo("Choose one");
            assertThat(msg.canDecline()).isTrue();
        }
    }

    // ========================================================================
    // beginTargetedCardChoice
    // ========================================================================

    @Nested
    @DisplayName("beginTargetedCardChoice")
    class BeginTargetedCardChoice {

        @Test
        @DisplayName("Sets interaction state to TARGETED_CARD_CHOICE")
        void setsInteractionState() {
            UUID targetId = UUID.randomUUID();

            svc.beginTargetedCardChoice(gd, PLAYER1_ID, List.of(0), "Choose", targetId);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.TARGETED_CARD_CHOICE);
        }

        @Test
        @DisplayName("Sends message to correct player")
        void sendsMessage() {
            UUID targetId = UUID.randomUUID();

            svc.beginTargetedCardChoice(gd, PLAYER2_ID, List.of(1, 3), "Choose card", targetId);

            verify(sessionManager).sendToPlayer(eq(PLAYER2_ID), any(ChooseCardFromHandMessage.class));
        }
    }

    // ========================================================================
    // beginPermanentChoice
    // ========================================================================

    @Nested
    @DisplayName("beginPermanentChoice")
    class BeginPermanentChoice {

        @Test
        @DisplayName("Sets interaction state to PERMANENT_CHOICE")
        void setsInteractionState() {
            UUID permId = UUID.randomUUID();

            svc.beginPermanentChoice(gd, PLAYER1_ID, List.of(permId), "Pick a permanent");

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        }

        @Test
        @DisplayName("Sends ChoosePermanentMessage with valid IDs")
        void sendsMessage() {
            UUID perm1 = UUID.randomUUID();
            UUID perm2 = UUID.randomUUID();

            svc.beginPermanentChoice(gd, PLAYER1_ID, List.of(perm1, perm2), "Choose permanent");

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChoosePermanentMessage msg = (ChoosePermanentMessage) messageCaptor.getValue();
            assertThat(msg.permanentIds()).containsExactly(perm1, perm2);
            assertThat(msg.prompt()).isEqualTo("Choose permanent");
        }
    }

    // ========================================================================
    // beginAnyTargetChoice
    // ========================================================================

    @Nested
    @DisplayName("beginAnyTargetChoice")
    class BeginAnyTargetChoice {

        @Test
        @DisplayName("Sets interaction state to PERMANENT_CHOICE")
        void setsInteractionState() {
            UUID permId = UUID.randomUUID();

            svc.beginAnyTargetChoice(gd, PLAYER1_ID, List.of(permId), List.of(PLAYER2_ID), "Choose target");

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        }

        @Test
        @DisplayName("Sends message with both permanent and player IDs")
        void sendsMessageWithBothTargetTypes() {
            UUID permId = UUID.randomUUID();

            svc.beginAnyTargetChoice(gd, PLAYER1_ID, List.of(permId), List.of(PLAYER2_ID), "Choose any");

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChoosePermanentMessage msg = (ChoosePermanentMessage) messageCaptor.getValue();
            assertThat(msg.permanentIds()).containsExactly(permId);
            assertThat(msg.playerIds()).containsExactly(PLAYER2_ID);
        }

        @Test
        @DisplayName("Combines permanent and player IDs in interaction valid set")
        void combinesValidIds() {
            UUID permId = UUID.randomUUID();

            svc.beginAnyTargetChoice(gd, PLAYER1_ID, List.of(permId), List.of(PLAYER2_ID), "Choose any");

            assertThat(gd.interaction.permanentChoice().validIds()).containsExactlyInAnyOrder(permId, PLAYER2_ID);
        }
    }

    // ========================================================================
    // beginGraveyardChoice
    // ========================================================================

    @Nested
    @DisplayName("beginGraveyardChoice")
    class BeginGraveyardChoice {

        @Test
        @DisplayName("Sets interaction state to GRAVEYARD_CHOICE")
        void setsInteractionState() {
            gd.interaction.prepareGraveyardChoice(GraveyardChoiceDestination.HAND, null);

            svc.beginGraveyardChoice(gd, PLAYER1_ID, List.of(0, 1), "Choose from graveyard");

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        }

        @Test
        @DisplayName("Sends allGraveyards=true when cardPool is non-null")
        void sendsAllGraveyardsTrueWhenCardPoolPresent() {
            Card card = createCreature("Test Card");
            gd.interaction.prepareGraveyardChoice(GraveyardChoiceDestination.BATTLEFIELD, List.of(card));

            svc.beginGraveyardChoice(gd, PLAYER1_ID, List.of(0), "Choose");

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseCardFromGraveyardMessage msg = (ChooseCardFromGraveyardMessage) messageCaptor.getValue();
            assertThat(msg.allGraveyards()).isTrue();
        }

        @Test
        @DisplayName("Sends allGraveyards=false when cardPool is null")
        void sendsAllGraveyardsFalseWhenNoCardPool() {
            gd.interaction.prepareGraveyardChoice(GraveyardChoiceDestination.HAND, null);

            svc.beginGraveyardChoice(gd, PLAYER1_ID, List.of(0, 1), "Choose");

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseCardFromGraveyardMessage msg = (ChooseCardFromGraveyardMessage) messageCaptor.getValue();
            assertThat(msg.allGraveyards()).isFalse();
        }
    }

    // ========================================================================
    // beginMultiPermanentChoice
    // ========================================================================

    @Nested
    @DisplayName("beginMultiPermanentChoice")
    class BeginMultiPermanentChoice {

        @Test
        @DisplayName("Sets interaction state to MULTI_PERMANENT_CHOICE")
        void setsInteractionState() {
            UUID perm1 = UUID.randomUUID();

            svc.beginMultiPermanentChoice(gd, PLAYER1_ID, List.of(perm1), 3, "Choose up to 3");

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        }

        @Test
        @DisplayName("Sends ChooseMultiplePermanentsMessage with correct maxCount")
        void sendsMessageWithMaxCount() {
            UUID perm1 = UUID.randomUUID();
            UUID perm2 = UUID.randomUUID();

            svc.beginMultiPermanentChoice(gd, PLAYER1_ID, List.of(perm1, perm2), 2, "Pick");

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseMultiplePermanentsMessage msg = (ChooseMultiplePermanentsMessage) messageCaptor.getValue();
            assertThat(msg.permanentIds()).containsExactly(perm1, perm2);
            assertThat(msg.maxCount()).isEqualTo(2);
        }
    }

    // ========================================================================
    // beginMultiGraveyardChoice
    // ========================================================================

    @Nested
    @DisplayName("beginMultiGraveyardChoice")
    class BeginMultiGraveyardChoice {

        @Test
        @DisplayName("Sets interaction state to MULTI_GRAVEYARD_CHOICE")
        void setsInteractionState() {
            UUID cardId = UUID.randomUUID();
            CardView cardView = mock(CardView.class);

            svc.beginMultiGraveyardChoice(gd, PLAYER1_ID, List.of(cardId), List.of(cardView), 2, "Choose cards");

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        }

        @Test
        @DisplayName("Sends ChooseMultipleCardsFromGraveyardsMessage")
        void sendsMessage() {
            UUID cardId = UUID.randomUUID();
            CardView cardView = mock(CardView.class);

            svc.beginMultiGraveyardChoice(gd, PLAYER1_ID, List.of(cardId), List.of(cardView), 5, "Choose");

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseMultipleCardsFromGraveyardsMessage msg = (ChooseMultipleCardsFromGraveyardsMessage) messageCaptor.getValue();
            assertThat(msg.cardIds()).containsExactly(cardId);
            assertThat(msg.maxCount()).isEqualTo(5);
        }
    }

    // ========================================================================
    // beginColorChoice
    // ========================================================================

    @Nested
    @DisplayName("beginColorChoice")
    class BeginColorChoice {

        @Test
        @DisplayName("Sets interaction state to COLOR_CHOICE")
        void setsInteractionState() {
            UUID permId = UUID.randomUUID();

            svc.beginColorChoice(gd, PLAYER1_ID, permId, null);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        }

        @Test
        @DisplayName("Sends five color options")
        void sendsFiveColors() {
            svc.beginColorChoice(gd, PLAYER1_ID, UUID.randomUUID(), null);

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseFromListMessage msg = (ChooseFromListMessage) messageCaptor.getValue();
            assertThat(msg.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");
            assertThat(msg.prompt()).isEqualTo("Choose a color.");
        }
    }

    // ========================================================================
    // beginProtectionColorChoice
    // ========================================================================

    @Nested
    @DisplayName("beginProtectionColorChoice")
    class BeginProtectionColorChoice {

        @Test
        @DisplayName("Includes ARTIFACT option when includeArtifacts=true")
        void includesArtifactOption() {
            UUID targetId = UUID.randomUUID();

            svc.beginProtectionColorChoice(gd, PLAYER1_ID, targetId, true);

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseFromListMessage msg = (ChooseFromListMessage) messageCaptor.getValue();
            assertThat(msg.options()).containsExactly("ARTIFACT", "WHITE", "BLUE", "BLACK", "RED", "GREEN");
            assertThat(msg.prompt()).isEqualTo("Choose a color or artifacts.");
        }

        @Test
        @DisplayName("Excludes ARTIFACT option when includeArtifacts=false")
        void excludesArtifactOption() {
            UUID targetId = UUID.randomUUID();

            svc.beginProtectionColorChoice(gd, PLAYER1_ID, targetId, false);

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseFromListMessage msg = (ChooseFromListMessage) messageCaptor.getValue();
            assertThat(msg.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");
            assertThat(msg.prompt()).isEqualTo("Choose a color.");
        }

        @Test
        @DisplayName("Stores ProtectionColorChoice context")
        void storesContext() {
            UUID targetId = UUID.randomUUID();

            svc.beginProtectionColorChoice(gd, PLAYER1_ID, targetId, true);

            assertThat(gd.interaction.colorChoiceContext()).isInstanceOf(ChoiceContext.ProtectionColorChoice.class);
            ChoiceContext.ProtectionColorChoice ctx = (ChoiceContext.ProtectionColorChoice) gd.interaction.colorChoiceContext();
            assertThat(ctx.targetId()).isEqualTo(targetId);
            assertThat(ctx.includeArtifacts()).isTrue();
        }
    }

    // ========================================================================
    // beginKeywordChoice
    // ========================================================================

    @Nested
    @DisplayName("beginKeywordChoice")
    class BeginKeywordChoice {

        @Test
        @DisplayName("Sends keyword names as options")
        void sendsKeywordNames() {
            UUID targetId = UUID.randomUUID();
            List<Keyword> options = List.of(Keyword.FLYING, Keyword.TRAMPLE, Keyword.LIFELINK);

            svc.beginKeywordChoice(gd, PLAYER1_ID, targetId, options);

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseFromListMessage msg = (ChooseFromListMessage) messageCaptor.getValue();
            assertThat(msg.options()).containsExactly("FLYING", "TRAMPLE", "LIFELINK");
            assertThat(msg.prompt()).isEqualTo("Choose a keyword to grant.");
        }

        @Test
        @DisplayName("Stores KeywordGrantChoice context")
        void storesContext() {
            UUID targetId = UUID.randomUUID();
            List<Keyword> options = List.of(Keyword.FLYING, Keyword.FIRST_STRIKE);

            svc.beginKeywordChoice(gd, PLAYER1_ID, targetId, options);

            assertThat(gd.interaction.colorChoiceContext()).isInstanceOf(ChoiceContext.KeywordGrantChoice.class);
            ChoiceContext.KeywordGrantChoice ctx = (ChoiceContext.KeywordGrantChoice) gd.interaction.colorChoiceContext();
            assertThat(ctx.targetId()).isEqualTo(targetId);
            assertThat(ctx.options()).containsExactly(Keyword.FLYING, Keyword.FIRST_STRIKE);
        }
    }

    // ========================================================================
    // beginSubtypeChoice
    // ========================================================================

    @Nested
    @DisplayName("beginSubtypeChoice")
    class BeginSubtypeChoice {

        @Test
        @DisplayName("Excludes non-creature subtypes from options")
        void excludesNonCreatureSubtypes() {
            UUID permId = UUID.randomUUID();

            svc.beginSubtypeChoice(gd, PLAYER1_ID, permId);

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseFromListMessage msg = (ChooseFromListMessage) messageCaptor.getValue();
            assertThat(msg.options()).doesNotContain("FOREST", "MOUNTAIN", "ISLAND", "PLAINS", "SWAMP", "AURA", "EQUIPMENT", "LOCUS");
            assertThat(msg.prompt()).isEqualTo("Choose a creature type.");
        }

        @Test
        @DisplayName("Stores SubtypeChoice context")
        void storesContext() {
            UUID permId = UUID.randomUUID();

            svc.beginSubtypeChoice(gd, PLAYER1_ID, permId);

            assertThat(gd.interaction.colorChoiceContext()).isInstanceOf(ChoiceContext.SubtypeChoice.class);
            ChoiceContext.SubtypeChoice ctx = (ChoiceContext.SubtypeChoice) gd.interaction.colorChoiceContext();
            assertThat(ctx.permanentId()).isEqualTo(permId);
        }
    }

    // ========================================================================
    // beginPermanentTypeChoice
    // ========================================================================

    @Nested
    @DisplayName("beginPermanentTypeChoice")
    class BeginPermanentTypeChoice {

        @Test
        @DisplayName("Sends five permanent type options")
        void sendsPermanentTypes() {
            svc.beginPermanentTypeChoice(gd, PLAYER1_ID, GraveyardChoiceDestination.BATTLEFIELD, "some desc");

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseFromListMessage msg = (ChooseFromListMessage) messageCaptor.getValue();
            assertThat(msg.options()).containsExactly("ARTIFACT", "CREATURE", "ENCHANTMENT", "LAND", "PLANESWALKER");
            assertThat(msg.prompt()).isEqualTo("Choose a permanent type.");
        }

        @Test
        @DisplayName("Stores PermanentTypeChoice context with destination and description")
        void storesContext() {
            svc.beginPermanentTypeChoice(gd, PLAYER1_ID, GraveyardChoiceDestination.HAND, "entry desc");

            assertThat(gd.interaction.colorChoiceContext()).isInstanceOf(ChoiceContext.PermanentTypeChoice.class);
            ChoiceContext.PermanentTypeChoice ctx = (ChoiceContext.PermanentTypeChoice) gd.interaction.colorChoiceContext();
            assertThat(ctx.controllerId()).isEqualTo(PLAYER1_ID);
            assertThat(ctx.destination()).isEqualTo(GraveyardChoiceDestination.HAND);
            assertThat(ctx.entryDescription()).isEqualTo("entry desc");
        }
    }

    // ========================================================================
    // beginBasicLandTypeChoice
    // ========================================================================

    @Nested
    @DisplayName("beginBasicLandTypeChoice")
    class BeginBasicLandTypeChoice {

        @Test
        @DisplayName("Sends five basic land type options")
        void sendsBasicLandTypes() {
            UUID permId = UUID.randomUUID();

            svc.beginBasicLandTypeChoice(gd, PLAYER1_ID, permId);

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseFromListMessage msg = (ChooseFromListMessage) messageCaptor.getValue();
            assertThat(msg.options()).containsExactly("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");
            assertThat(msg.prompt()).isEqualTo("Choose a basic land type.");
        }

        @Test
        @DisplayName("Stores BasicLandTypeChoice context")
        void storesContext() {
            UUID permId = UUID.randomUUID();

            svc.beginBasicLandTypeChoice(gd, PLAYER1_ID, permId);

            assertThat(gd.interaction.colorChoiceContext()).isInstanceOf(ChoiceContext.BasicLandTypeChoice.class);
            ChoiceContext.BasicLandTypeChoice ctx = (ChoiceContext.BasicLandTypeChoice) gd.interaction.colorChoiceContext();
            assertThat(ctx.permanentId()).isEqualTo(permId);
        }
    }

    // ========================================================================
    // beginCardNameChoice
    // ========================================================================

    @Nested
    @DisplayName("beginCardNameChoice")
    class BeginCardNameChoice {

        @Test
        @DisplayName("Collects card names from all zones when no exclusions")
        void collectsFromAllZones() {
            Card handCard = createCreature("Alpha");
            Card bfCard = createCreature("Bravo");
            Card gyCard = createCreature("Charlie");
            Card deckCard = createCreature("Delta");
            Card exiledCard = createCreature("Echo");
            Card sourceCard = createCreature("Source");

            gd.playerHands.get(PLAYER1_ID).add(handCard);
            gd.playerBattlefields.get(PLAYER1_ID).add(new Permanent(bfCard));
            gd.playerGraveyards.get(PLAYER1_ID).add(gyCard);
            gd.playerDecks.get(PLAYER1_ID).add(deckCard);
            gd.playerExiledCards.get(PLAYER1_ID).add(exiledCard);

            svc.beginCardNameChoice(gd, PLAYER1_ID, sourceCard, List.of());

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseFromListMessage msg = (ChooseFromListMessage) messageCaptor.getValue();
            assertThat(msg.options()).contains("Alpha", "Bravo", "Charlie", "Delta", "Echo");
            assertThat(msg.prompt()).isEqualTo("Choose a card name.");
        }

        @Test
        @DisplayName("Collects card names from the stack")
        void collectsFromStack() {
            Card stackCard = createCreature("StackCreature");
            Card sourceCard = createCreature("Source");
            gd.stack.add(new StackEntry(stackCard, PLAYER1_ID));

            svc.beginCardNameChoice(gd, PLAYER1_ID, sourceCard, List.of());

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseFromListMessage msg = (ChooseFromListMessage) messageCaptor.getValue();
            assertThat(msg.options()).contains("StackCreature");
        }

        @Test
        @DisplayName("Excludes cards with excluded types")
        void excludesCardsByType() {
            Card creature = createCreature("Bear");
            Card land = createCard("Mountain", CardType.LAND);
            Card sourceCard = createCreature("Source");

            gd.playerHands.get(PLAYER1_ID).add(creature);
            gd.playerHands.get(PLAYER1_ID).add(land);

            svc.beginCardNameChoice(gd, PLAYER1_ID, sourceCard, List.of(CardType.LAND));

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseFromListMessage msg = (ChooseFromListMessage) messageCaptor.getValue();
            assertThat(msg.options()).contains("Bear");
            assertThat(msg.options()).doesNotContain("Mountain");
            assertThat(msg.prompt()).isEqualTo("Choose a nonland card name.");
        }

        @Test
        @DisplayName("Excludes cards with matching additional types")
        void excludesAdditionalTypes() {
            Card artifactCreature = createCreature("Golem");
            artifactCreature.setAdditionalTypes(Set.of(CardType.ARTIFACT));
            Card pureCreature = createCreature("Bear");
            Card sourceCard = createCreature("Source");

            gd.playerHands.get(PLAYER1_ID).add(artifactCreature);
            gd.playerHands.get(PLAYER1_ID).add(pureCreature);

            svc.beginCardNameChoice(gd, PLAYER1_ID, sourceCard, List.of(CardType.ARTIFACT));

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseFromListMessage msg = (ChooseFromListMessage) messageCaptor.getValue();
            assertThat(msg.options()).contains("Bear");
            assertThat(msg.options()).doesNotContain("Golem");
        }

        @Test
        @DisplayName("Returns sorted unique names")
        void returnsSortedUniqueNames() {
            Card card1 = createCreature("Zebra");
            Card card2 = createCreature("Alpha");
            Card card3 = createCreature("Alpha"); // duplicate
            Card sourceCard = createCreature("Source");

            gd.playerHands.get(PLAYER1_ID).add(card1);
            gd.playerHands.get(PLAYER1_ID).add(card2);
            gd.playerHands.get(PLAYER2_ID).add(card3);

            svc.beginCardNameChoice(gd, PLAYER1_ID, sourceCard, List.of());

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseFromListMessage msg = (ChooseFromListMessage) messageCaptor.getValue();
            assertThat(msg.options()).doesNotHaveDuplicates();
            // Names should be sorted alphabetically (TreeSet)
            int alphaIdx = msg.options().indexOf("Alpha");
            int zebraIdx = msg.options().indexOf("Zebra");
            assertThat(alphaIdx).isLessThan(zebraIdx);
        }

        @Test
        @DisplayName("Stores CardNameChoice context")
        void storesContext() {
            Card sourceCard = createCreature("Source");
            List<CardType> excluded = List.of(CardType.LAND);

            svc.beginCardNameChoice(gd, PLAYER1_ID, sourceCard, excluded);

            assertThat(gd.interaction.colorChoiceContext()).isInstanceOf(ChoiceContext.CardNameChoice.class);
            ChoiceContext.CardNameChoice ctx = (ChoiceContext.CardNameChoice) gd.interaction.colorChoiceContext();
            assertThat(ctx.card()).isEqualTo(sourceCard);
            assertThat(ctx.controllerId()).isEqualTo(PLAYER1_ID);
            assertThat(ctx.excludedTypes()).containsExactly(CardType.LAND);
        }
    }

    // ========================================================================
    // beginSpellCardNameChoice
    // ========================================================================

    @Nested
    @DisplayName("beginSpellCardNameChoice")
    class BeginSpellCardNameChoice {

        @Test
        @DisplayName("Sends to choosingPlayerId via mind control redirect")
        void sendsToChoosingPlayer() {
            Card card = createCreature("Bear");
            gd.playerHands.get(PLAYER2_ID).add(card);

            svc.beginSpellCardNameChoice(gd, PLAYER1_ID, PLAYER2_ID, List.of(CardType.LAND));

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), any(ChooseFromListMessage.class));
        }

        @Test
        @DisplayName("Stores ExileByNameChoice context")
        void storesContext() {
            svc.beginSpellCardNameChoice(gd, PLAYER1_ID, PLAYER2_ID, List.of(CardType.LAND));

            assertThat(gd.interaction.colorChoiceContext()).isInstanceOf(ChoiceContext.ExileByNameChoice.class);
            ChoiceContext.ExileByNameChoice ctx = (ChoiceContext.ExileByNameChoice) gd.interaction.colorChoiceContext();
            assertThat(ctx.targetPlayerId()).isEqualTo(PLAYER2_ID);
            assertThat(ctx.controllerId()).isEqualTo(PLAYER1_ID);
        }
    }

    // ========================================================================
    // beginSphinxAmbassadorCardNameChoice
    // ========================================================================

    @Nested
    @DisplayName("beginSphinxAmbassadorCardNameChoice")
    class BeginSphinxAmbassadorCardNameChoice {

        @Test
        @DisplayName("Collects all card names and sends to naming player")
        void collectsAllNames() {
            Card card = createCreature("Sphinx");
            gd.playerHands.get(PLAYER2_ID).add(card);

            svc.beginSphinxAmbassadorCardNameChoice(gd, PLAYER2_ID, PLAYER1_ID);

            verify(sessionManager).sendToPlayer(eq(PLAYER2_ID), messageCaptor.capture());
            ChooseFromListMessage msg = (ChooseFromListMessage) messageCaptor.getValue();
            assertThat(msg.options()).contains("Sphinx");
            assertThat(msg.prompt()).isEqualTo("Choose a card name.");
        }

        @Test
        @DisplayName("Stores SphinxAmbassadorNameChoice context")
        void storesContext() {
            svc.beginSphinxAmbassadorCardNameChoice(gd, PLAYER2_ID, PLAYER1_ID);

            assertThat(gd.interaction.colorChoiceContext()).isInstanceOf(ChoiceContext.SphinxAmbassadorNameChoice.class);
            ChoiceContext.SphinxAmbassadorNameChoice ctx = (ChoiceContext.SphinxAmbassadorNameChoice) gd.interaction.colorChoiceContext();
            assertThat(ctx.namingPlayerId()).isEqualTo(PLAYER2_ID);
            assertThat(ctx.controllerId()).isEqualTo(PLAYER1_ID);
        }
    }

    // ========================================================================
    // beginMultiZoneExileChoice
    // ========================================================================

    @Nested
    @DisplayName("beginMultiZoneExileChoice")
    class BeginMultiZoneExileChoice {

        @Test
        @DisplayName("Sets interaction state to MULTI_ZONE_EXILE_CHOICE")
        void setsInteractionState() {
            Card card = createCreature("Bear");
            CardView cardView = mock(CardView.class);
            when(cardViewFactory.create(card)).thenReturn(cardView);

            svc.beginMultiZoneExileChoice(gd, PLAYER1_ID, List.of(card), PLAYER2_ID, "Bear");

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_ZONE_EXILE_CHOICE);
        }

        @Test
        @DisplayName("Sends correct message with card views")
        void sendsMessage() {
            Card card1 = createCreature("Bear");
            Card card2 = createCreature("Bear");
            CardView view1 = mock(CardView.class);
            CardView view2 = mock(CardView.class);
            when(cardViewFactory.create(card1)).thenReturn(view1);
            when(cardViewFactory.create(card2)).thenReturn(view2);

            svc.beginMultiZoneExileChoice(gd, PLAYER1_ID, List.of(card1, card2), PLAYER2_ID, "Bear");

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseMultipleCardsFromGraveyardsMessage msg = (ChooseMultipleCardsFromGraveyardsMessage) messageCaptor.getValue();
            assertThat(msg.maxCount()).isEqualTo(2);
            assertThat(msg.cards()).containsExactly(view1, view2);
            assertThat(msg.prompt()).contains("Bear");
        }
    }

    // ========================================================================
    // beginImprintFromHandChoice
    // ========================================================================

    @Nested
    @DisplayName("beginImprintFromHandChoice")
    class BeginImprintFromHandChoice {

        @Test
        @DisplayName("Sets interaction state to IMPRINT_FROM_HAND_CHOICE")
        void setsInteractionState() {
            UUID sourcePermId = UUID.randomUUID();

            svc.beginImprintFromHandChoice(gd, PLAYER1_ID, List.of(0, 1), "Choose artifact", sourcePermId);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.IMPRINT_FROM_HAND_CHOICE);
        }

        @Test
        @DisplayName("Sends ChooseCardFromHandMessage without canDecline")
        void sendsMessage() {
            UUID sourcePermId = UUID.randomUUID();

            svc.beginImprintFromHandChoice(gd, PLAYER1_ID, List.of(0, 2), "Choose to imprint", sourcePermId);

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseCardFromHandMessage msg = (ChooseCardFromHandMessage) messageCaptor.getValue();
            assertThat(msg.cardIndices()).containsExactly(0, 2);
            assertThat(msg.prompt()).isEqualTo("Choose to imprint");
            assertThat(msg.canDecline()).isFalse();
        }
    }

    // ========================================================================
    // beginExileFromHandChoice
    // ========================================================================

    @Nested
    @DisplayName("beginExileFromHandChoice")
    class BeginExileFromHandChoice {

        @Test
        @DisplayName("Sets interaction state to EXILE_FROM_HAND_CHOICE")
        void setsInteractionState() {
            gd.playerHands.get(PLAYER1_ID).addAll(List.of(createCreature("A"), createCreature("B")));
            UUID sourcePermId = UUID.randomUUID();

            svc.beginExileFromHandChoice(gd, PLAYER1_ID, sourcePermId);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.EXILE_FROM_HAND_CHOICE);
        }

        @Test
        @DisplayName("Generates valid indices for all cards in hand")
        void generatesValidIndicesForEntireHand() {
            gd.playerHands.get(PLAYER1_ID).addAll(List.of(createCreature("A"), createCreature("B"), createCreature("C")));
            UUID sourcePermId = UUID.randomUUID();

            svc.beginExileFromHandChoice(gd, PLAYER1_ID, sourcePermId);

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseCardFromHandMessage msg = (ChooseCardFromHandMessage) messageCaptor.getValue();
            assertThat(msg.cardIndices()).containsExactly(0, 1, 2);
            assertThat(msg.prompt()).isEqualTo("Choose a card to exile.");
        }
    }

    // ========================================================================
    // beginDiscardChoice
    // ========================================================================

    @Nested
    @DisplayName("beginDiscardChoice")
    class BeginDiscardChoice {

        @Test
        @DisplayName("No-args version generates indices for entire hand")
        void noArgsGeneratesIndices() {
            gd.playerHands.get(PLAYER1_ID).addAll(List.of(createCreature("A"), createCreature("B")));

            svc.beginDiscardChoice(gd, PLAYER1_ID);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseCardFromHandMessage msg = (ChooseCardFromHandMessage) messageCaptor.getValue();
            assertThat(msg.cardIndices()).containsExactly(0, 1);
            assertThat(msg.prompt()).isEqualTo("Choose a card to discard.");
        }

        @Test
        @DisplayName("Parameterized version uses provided indices and prompt")
        void parameterizedVersionUsesProvidedArgs() {
            svc.beginDiscardChoice(gd, PLAYER1_ID, List.of(1, 3), "Discard a land");

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseCardFromHandMessage msg = (ChooseCardFromHandMessage) messageCaptor.getValue();
            assertThat(msg.cardIndices()).containsExactly(1, 3);
            assertThat(msg.prompt()).isEqualTo("Discard a land");
        }
    }

    // ========================================================================
    // beginRevealedHandChoice
    // ========================================================================

    @Nested
    @DisplayName("beginRevealedHandChoice")
    class BeginRevealedHandChoice {

        @Test
        @DisplayName("Sets interaction state to REVEALED_HAND_CHOICE")
        void setsInteractionState() {
            Card handCard = createCreature("Bear");
            gd.playerHands.get(PLAYER2_ID).add(handCard);
            CardView cardView = mock(CardView.class);
            when(cardViewFactory.create(handCard)).thenReturn(cardView);
            // Need to pre-initialize revealedHandChoice state
            gd.interaction.beginRevealedHandChoice(PLAYER1_ID, PLAYER2_ID, Set.of(0), 1, false, List.of());

            svc.beginRevealedHandChoice(gd, PLAYER1_ID, PLAYER2_ID, List.of(0), "Choose one");

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.REVEALED_HAND_CHOICE);
        }

        @Test
        @DisplayName("Creates card views for target player's hand and sends message")
        void sendsMessageWithCardViews() {
            Card card1 = createCreature("Bear");
            Card card2 = createCreature("Wolf");
            gd.playerHands.get(PLAYER2_ID).addAll(List.of(card1, card2));
            CardView view1 = mock(CardView.class);
            CardView view2 = mock(CardView.class);
            when(cardViewFactory.create(card1)).thenReturn(view1);
            when(cardViewFactory.create(card2)).thenReturn(view2);
            gd.interaction.beginRevealedHandChoice(PLAYER1_ID, PLAYER2_ID, Set.of(0, 1), 1, false, List.of());

            svc.beginRevealedHandChoice(gd, PLAYER1_ID, PLAYER2_ID, List.of(0, 1), "Pick one");

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseFromRevealedHandMessage msg = (ChooseFromRevealedHandMessage) messageCaptor.getValue();
            assertThat(msg.cards()).containsExactly(view1, view2);
            assertThat(msg.validIndices()).containsExactly(0, 1);
            assertThat(msg.prompt()).isEqualTo("Pick one");
        }
    }

    // ========================================================================
    // beginXValueChoice
    // ========================================================================

    @Nested
    @DisplayName("beginXValueChoice")
    class BeginXValueChoice {

        @Test
        @DisplayName("Sets interaction state to X_VALUE_CHOICE")
        void setsInteractionState() {
            svc.beginXValueChoice(gd, PLAYER1_ID, 5, "Choose X", "Fireball");

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.X_VALUE_CHOICE);
        }

        @Test
        @DisplayName("Sends XValueChoiceMessage with correct parameters")
        void sendsMessage() {
            svc.beginXValueChoice(gd, PLAYER1_ID, 10, "Choose X value", "Blaze");

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            XValueChoiceMessage msg = (XValueChoiceMessage) messageCaptor.getValue();
            assertThat(msg.maxValue()).isEqualTo(10);
            assertThat(msg.prompt()).isEqualTo("Choose X value");
            assertThat(msg.cardName()).isEqualTo("Blaze");
        }
    }

    // ========================================================================
    // sendKnowledgePoolCastChoice
    // ========================================================================

    @Nested
    @DisplayName("sendKnowledgePoolCastChoice")
    class SendKnowledgePoolCastChoice {

        @Test
        @DisplayName("Sends message with maxCount 1 and correct prompt")
        void sendsMessage() {
            UUID cardId = UUID.randomUUID();
            CardView cardView = mock(CardView.class);

            svc.sendKnowledgePoolCastChoice(gd, PLAYER1_ID, List.of(cardId), List.of(cardView));

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseMultipleCardsFromGraveyardsMessage msg = (ChooseMultipleCardsFromGraveyardsMessage) messageCaptor.getValue();
            assertThat(msg.maxCount()).isEqualTo(1);
            assertThat(msg.prompt()).contains("Knowledge Pool");
        }
    }

    // ========================================================================
    // sendMirrorOfFateChoice
    // ========================================================================

    @Nested
    @DisplayName("sendMirrorOfFateChoice")
    class SendMirrorOfFateChoice {

        @Test
        @DisplayName("Sends message with correct maxCount and prompt")
        void sendsMessage() {
            UUID cardId = UUID.randomUUID();
            CardView cardView = mock(CardView.class);

            svc.sendMirrorOfFateChoice(gd, PLAYER1_ID, List.of(cardId), List.of(cardView), 7);

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseMultipleCardsFromGraveyardsMessage msg = (ChooseMultipleCardsFromGraveyardsMessage) messageCaptor.getValue();
            assertThat(msg.maxCount()).isEqualTo(7);
            assertThat(msg.prompt()).contains("seven");
        }
    }

    // ========================================================================
    // beginLibraryReorderFromExile
    // ========================================================================

    @Nested
    @DisplayName("beginLibraryReorderFromExile")
    class BeginLibraryReorderFromExile {

        @Test
        @DisplayName("Sets interaction state to LIBRARY_REORDER")
        void setsInteractionState() {
            Card card = createCreature("Bear");
            CardView cardView = mock(CardView.class);
            when(cardViewFactory.create(card)).thenReturn(cardView);

            svc.beginLibraryReorderFromExile(gd, PLAYER1_ID, List.of(card));

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        }

        @Test
        @DisplayName("Sends ReorderLibraryCardsMessage with card views")
        void sendsMessage() {
            Card card1 = createCreature("Alpha");
            Card card2 = createCreature("Beta");
            CardView view1 = mock(CardView.class);
            CardView view2 = mock(CardView.class);
            when(cardViewFactory.create(card1)).thenReturn(view1);
            when(cardViewFactory.create(card2)).thenReturn(view2);

            svc.beginLibraryReorderFromExile(gd, PLAYER1_ID, List.of(card1, card2));

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ReorderLibraryCardsMessage msg = (ReorderLibraryCardsMessage) messageCaptor.getValue();
            assertThat(msg.cards()).containsExactly(view1, view2);
            assertThat(msg.prompt()).contains("top of your library");
        }
    }

    // ========================================================================
    // processNextMayAbility
    // ========================================================================

    @Nested
    @DisplayName("processNextMayAbility")
    class ProcessNextMayAbility {

        @Test
        @DisplayName("Does nothing when pendingMayAbilities is empty")
        void doesNothingWhenEmpty() {
            svc.processNextMayAbility(gd);

            verifyNoInteractions(sessionManager);
            assertThat(gd.interaction.awaitingInputType()).isNull();
        }

        @Test
        @DisplayName("Clears pending abilities and does nothing when game is FINISHED")
        void clearsAndDoesNothingWhenFinished() {
            gd.status = GameStatus.FINISHED;
            Card card = createCreature("Source");
            gd.pendingMayAbilities.add(new PendingMayAbility(card, PLAYER1_ID, List.of(), "May draw a card"));

            svc.processNextMayAbility(gd);

            assertThat(gd.pendingMayAbilities).isEmpty();
            verifyNoInteractions(sessionManager);
        }

        @Test
        @DisplayName("Sends MayAbilityMessage for first pending ability without mana cost")
        void sendsMessageForAbilityWithoutManaCost() {
            Card card = createCreature("Source");
            gd.pendingMayAbilities.add(new PendingMayAbility(card, PLAYER1_ID, List.of(), "May draw a card"));

            svc.processNextMayAbility(gd);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            MayAbilityMessage msg = (MayAbilityMessage) messageCaptor.getValue();
            assertThat(msg.prompt()).isEqualTo("May draw a card");
            assertThat(msg.canPay()).isTrue();
            assertThat(msg.manaCost()).isNull();
        }

        @Test
        @DisplayName("Reports canPay=true when player can afford mana cost")
        void reportsCanPayTrueWhenAffordable() {
            Card card = createCreature("Source");
            gd.pendingMayAbilities.add(new PendingMayAbility(card, PLAYER1_ID, List.of(), "Pay {1}{U}", null, "{1}{U}"));
            // Give player enough mana
            gd.playerManaPools.get(PLAYER1_ID).add(ManaColor.BLUE, 1);
            gd.playerManaPools.get(PLAYER1_ID).add(ManaColor.WHITE, 1);

            svc.processNextMayAbility(gd);

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            MayAbilityMessage msg = (MayAbilityMessage) messageCaptor.getValue();
            assertThat(msg.canPay()).isTrue();
            assertThat(msg.manaCost()).isEqualTo("{1}{U}");
        }

        @Test
        @DisplayName("Reports canPay=false when player cannot afford mana cost")
        void reportsCanPayFalseWhenUnaffordable() {
            Card card = createCreature("Source");
            gd.pendingMayAbilities.add(new PendingMayAbility(card, PLAYER1_ID, List.of(), "Pay {3}{B}{B}", null, "{3}{B}{B}"));
            // Player has no mana

            svc.processNextMayAbility(gd);

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            MayAbilityMessage msg = (MayAbilityMessage) messageCaptor.getValue();
            assertThat(msg.canPay()).isFalse();
        }

        @Test
        @DisplayName("Reports canPay=true for X cost when player has any mana")
        void reportsCanPayForXCostWithMana() {
            Card card = createCreature("Source");
            gd.pendingMayAbilities.add(new PendingMayAbility(card, PLAYER1_ID, List.of(), "Pay {X}", null, "{X}"));
            gd.playerManaPools.get(PLAYER1_ID).add(ManaColor.RED, 1);

            svc.processNextMayAbility(gd);

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            MayAbilityMessage msg = (MayAbilityMessage) messageCaptor.getValue();
            assertThat(msg.canPay()).isTrue();
        }

        @Test
        @DisplayName("Reports canPay=false for X cost when player has no mana")
        void reportsCanPayFalseForXCostWithNoMana() {
            Card card = createCreature("Source");
            gd.pendingMayAbilities.add(new PendingMayAbility(card, PLAYER1_ID, List.of(), "Pay {X}", null, "{X}"));

            svc.processNextMayAbility(gd);

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            MayAbilityMessage msg = (MayAbilityMessage) messageCaptor.getValue();
            assertThat(msg.canPay()).isFalse();
        }

        @Test
        @DisplayName("Redirects to controller when player is mind-controlled")
        void redirectsMindControlled() {
            UUID controllerId = UUID.randomUUID();
            gd.playerIdToName.put(controllerId, "Controller");
            gd.mindControlledPlayerId = PLAYER1_ID;
            gd.mindControllerPlayerId = controllerId;

            Card card = createCreature("Source");
            gd.pendingMayAbilities.add(new PendingMayAbility(card, PLAYER1_ID, List.of(), "May do something"));

            svc.processNextMayAbility(gd);

            verify(sessionManager).sendToPlayer(eq(controllerId), any(MayAbilityMessage.class));
        }
    }
}
