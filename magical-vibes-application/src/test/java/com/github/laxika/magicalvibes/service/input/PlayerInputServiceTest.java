package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.event.InteractionPromptProjectionRegistry;
import com.github.laxika.magicalvibes.service.interaction.ColorChoiceInteractionHandler;
import com.github.laxika.magicalvibes.service.interaction.HandCardChoiceInteractionHandlers;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.interaction.MayAbilityChoiceInteractionHandler;
import com.github.laxika.magicalvibes.service.interaction.MultiGraveyardChoiceInteractionHandler;
import com.github.laxika.magicalvibes.service.interaction.MultiPermanentChoiceInteractionHandler;
import com.github.laxika.magicalvibes.service.interaction.MultiZoneExileChoiceInteractionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerInputServiceTest {

    @Mock private CardViewFactory cardViewFactory;

    private PlayerInputService svc;
    private InteractionPromptProjectionRegistry promptProjections;

    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        InteractionHandlerRegistry registry = new InteractionHandlerRegistry(() -> mock(
                com.github.laxika.magicalvibes.service.event.GameMutationCoordinator.class));
        registry.register(new MayAbilityChoiceInteractionHandler(
                mock(MayAbilityHandlerService.class)));
        registry.register(new MultiZoneExileChoiceInteractionHandler(
                mock(ChoiceHandlerService.class)));
        registry.register(new MultiPermanentChoiceInteractionHandler(
                mock(MultiPermanentChoiceHandlerService.class)));
        registry.register(new MultiGraveyardChoiceInteractionHandler(
                mock(GraveyardChoiceHandlerService.class)));
        registry.register(new ColorChoiceInteractionHandler(
                mock(ChoiceHandlerService.class)));
        CardChoiceHandlerService cardChoiceHandlerService = mock(CardChoiceHandlerService.class);
        registry.register(new HandCardChoiceInteractionHandlers.HandCardChoiceInteractionHandler(
                cardChoiceHandlerService));
        registry.register(new HandCardChoiceInteractionHandlers.TargetedHandCardChoiceInteractionHandler(
                cardChoiceHandlerService));
        registry.register(new HandCardChoiceInteractionHandlers.DiscardChoiceInteractionHandler(
                cardChoiceHandlerService));
        registry.register(new HandCardChoiceInteractionHandlers.ExileFromHandChoiceInteractionHandler(
                cardChoiceHandlerService));
        registry.register(new HandCardChoiceInteractionHandlers.ImprintFromHandChoiceInteractionHandler(
                cardChoiceHandlerService));
        registry.register(new com.github.laxika.magicalvibes.service.interaction.PermanentChoiceInteractionHandler(
                mock(PermanentChoiceHandlerService.class)));
        svc = new PlayerInputService(registry);
        promptProjections = new InteractionPromptProjectionRegistry(cardViewFactory);

        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Player1");
        gd.playerIdToName.put(PLAYER2_ID, "Player2");
        gd.playerBattlefields.put(PLAYER1_ID, new ArrayList<>());
        gd.playerBattlefields.put(PLAYER2_ID, new ArrayList<>());
        gd.playerGraveyards.put(PLAYER1_ID, new ArrayList<>());
        gd.playerGraveyards.put(PLAYER2_ID, new ArrayList<>());
        gd.playerDecks.put(PLAYER1_ID, new ArrayList<>());
        gd.playerDecks.put(PLAYER2_ID, new ArrayList<>());
        gd.playerHands.put(PLAYER1_ID, new ArrayList<>());
        gd.playerHands.put(PLAYER2_ID, new ArrayList<>());
        gd.playerManaPools.put(PLAYER1_ID, new ManaPool());
        gd.playerManaPools.put(PLAYER2_ID, new ManaPool());
    }

    private InteractionPromptMessage projectedPrompt() {
        return (InteractionPromptMessage) promptProjections
                .project(gd, gd.interaction.activeInteraction())
                .orElseThrow();
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
    // Decision ownership
    // ========================================================================

    @Nested
    @DisplayName("Decision ownership")
    class DecisionOwnership {

        @Test
        @DisplayName("Keeps the affected player as decision owner when mind-controlled")
        void keepsAffectedPlayerAsDecisionOwnerWhenMindControlled() {
            UUID controllerId = UUID.randomUUID();
            gd.playerIdToName.put(controllerId, "Controller");
            gd.mindControlledPlayerId = PLAYER1_ID;
            gd.mindControllerPlayerId = controllerId;

            svc.beginCardChoice(gd, PLAYER1_ID, List.of(0, 1), "Pick a card");

            assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(PLAYER1_ID);
            assertThat(projectedPrompt()).isNotNull();
        }

        @Test
        @DisplayName("Stores the choosing player as decision owner")
        void storesChoosingPlayerAsDecisionOwner() {
            svc.beginCardChoice(gd, PLAYER1_ID, List.of(0), "Pick a card");

            assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(PLAYER1_ID);
        }

        @Test
        @DisplayName("Another mind-controlled player does not change decision ownership")
        void otherMindControlledPlayerDoesNotChangeDecisionOwnership() {
            gd.mindControlledPlayerId = PLAYER2_ID;
            gd.mindControllerPlayerId = UUID.randomUUID();

            svc.beginCardChoice(gd, PLAYER1_ID, List.of(0), "Pick a card");

            assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(PLAYER1_ID);
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

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        }

        @Test
        @DisplayName("Sends correct message with valid indices and prompt")
        void sendsCorrectMessage() {
            List<Integer> indices = List.of(0, 2, 4);

            svc.beginCardChoice(gd, PLAYER1_ID, indices, "Choose one");

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.cardIndices()).containsExactly(0, 2, 4);
            assertThat(msg.prompt()).isEqualTo("Choose one");
            assertThat(msg.declinable()).isTrue();
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

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.TargetedHandCardChoice.class);
        }

        @Test
        @DisplayName("Stores the correct decision owner")
        void storesDecisionOwner() {
            UUID targetId = UUID.randomUUID();

            svc.beginTargetedCardChoice(gd, PLAYER2_ID, List.of(1, 3), "Choose card", targetId);

            assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(PLAYER2_ID);
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

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        }

        @Test
        @DisplayName("Sends InteractionPromptMessage with valid IDs")
        void sendsMessage() {
            UUID perm1 = UUID.randomUUID();
            UUID perm2 = UUID.randomUUID();

            svc.beginPermanentChoice(gd, PLAYER1_ID, List.of(perm1, perm2), "Choose permanent");

            InteractionPromptMessage msg = projectedPrompt();
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

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        }

        @Test
        @DisplayName("Sends message with both permanent and player IDs")
        void sendsMessageWithBothTargetTypes() {
            UUID permId = UUID.randomUUID();

            svc.beginAnyTargetChoice(gd, PLAYER1_ID, List.of(permId), List.of(PLAYER2_ID), "Choose any");

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.permanentIds()).containsExactly(permId);
            assertThat(msg.playerIds()).containsExactly(PLAYER2_ID);
        }

        @Test
        @DisplayName("Combines permanent and player IDs in interaction valid set")
        void combinesValidIds() {
            UUID permId = UUID.randomUUID();

            svc.beginAnyTargetChoice(gd, PLAYER1_ID, List.of(permId), List.of(PLAYER2_ID), "Choose any");

            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).containsExactlyInAnyOrder(permId, PLAYER2_ID);
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

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        }

        @Test
        @DisplayName("Sends InteractionPromptMessage with correct maxCount")
        void sendsMessageWithMaxCount() {
            UUID perm1 = UUID.randomUUID();
            UUID perm2 = UUID.randomUUID();

            svc.beginMultiPermanentChoice(gd, PLAYER1_ID, List.of(perm1, perm2), 2, "Pick");

            InteractionPromptMessage msg = projectedPrompt();
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
            Card card = createCreature("Grave Creature");

            svc.beginMultiGraveyardChoice(gd, PLAYER1_ID, List.of(card), 2, "Choose cards");

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        }

        @Test
        @DisplayName("Sends InteractionPromptMessage")
        void sendsMessage() {
            Card card = createCreature("Grave Creature");

            svc.beginMultiGraveyardChoice(gd, PLAYER1_ID, List.of(card), 5, "Choose");

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.cardIds()).containsExactly(card.getId());
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

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        }

        @Test
        @DisplayName("Sends five color options")
        void sendsFiveColors() {
            svc.beginColorChoice(gd, PLAYER1_ID, UUID.randomUUID(), null);

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");
            assertThat(msg.prompt()).isEqualTo("Choose a color.");
        }

        @Test
        @DisplayName("Stores the chosen-color life-gain context")
        void storesChosenColorLifeGainContext() {
            Card sourceCard = createCreature("Treva");
            svc.beginGainLifePerPermanentOfChosenColorChoice(gd, PLAYER1_ID, sourceCard,
                    com.github.laxika.magicalvibes.model.StackEntryType.TRIGGERED_ABILITY);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                    .isInstanceOf(ChoiceContext.GainLifePerPermanentOfChosenColorChoice.class);
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

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.options()).containsExactly("ARTIFACT", "WHITE", "BLUE", "BLACK", "RED", "GREEN");
            assertThat(msg.prompt()).isEqualTo("Choose a color or artifacts.");
        }

        @Test
        @DisplayName("Excludes ARTIFACT option when includeArtifacts=false")
        void excludesArtifactOption() {
            UUID targetId = UUID.randomUUID();

            svc.beginProtectionColorChoice(gd, PLAYER1_ID, targetId, false);

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");
            assertThat(msg.prompt()).isEqualTo("Choose a color.");
        }

        @Test
        @DisplayName("Stores ProtectionColorChoice context")
        void storesContext() {
            UUID targetId = UUID.randomUUID();

            svc.beginProtectionColorChoice(gd, PLAYER1_ID, targetId, true);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context()).isInstanceOf(ChoiceContext.ProtectionColorChoice.class);
            ChoiceContext.ProtectionColorChoice ctx = (ChoiceContext.ProtectionColorChoice) gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context();
            assertThat(ctx.targetIds()).containsExactly(targetId);
            assertThat(ctx.includeArtifacts()).isTrue();
        }

        @Test
        @DisplayName("Stores every target of a shared protection choice")
        void storesAllTargets() {
            UUID first = UUID.randomUUID();
            UUID second = UUID.randomUUID();

            svc.beginProtectionColorChoice(gd, PLAYER1_ID, List.of(first, second), false);

            ChoiceContext.ProtectionColorChoice ctx = (ChoiceContext.ProtectionColorChoice) gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context();
            assertThat(ctx.targetIds()).containsExactly(first, second);
            assertThat(ctx.includeArtifacts()).isFalse();
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

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.options()).containsExactly("FLYING", "TRAMPLE", "LIFELINK");
            assertThat(msg.prompt()).isEqualTo("Choose a keyword to grant.");
        }

        @Test
        @DisplayName("Stores KeywordGrantChoice context")
        void storesContext() {
            UUID targetId = UUID.randomUUID();
            List<Keyword> options = List.of(Keyword.FLYING, Keyword.FIRST_STRIKE);

            svc.beginKeywordChoice(gd, PLAYER1_ID, targetId, options);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context()).isInstanceOf(ChoiceContext.KeywordGrantChoice.class);
            ChoiceContext.KeywordGrantChoice ctx = (ChoiceContext.KeywordGrantChoice) gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context();
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

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.options()).doesNotContain("FOREST", "MOUNTAIN", "ISLAND", "PLAINS", "SWAMP", "AURA", "EQUIPMENT", "LOCUS");
            assertThat(msg.prompt()).isEqualTo("Choose a creature type.");
        }

        @Test
        @DisplayName("Stores SubtypeChoice context")
        void storesContext() {
            UUID permId = UUID.randomUUID();

            svc.beginSubtypeChoice(gd, PLAYER1_ID, permId);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context()).isInstanceOf(ChoiceContext.SubtypeChoice.class);
            ChoiceContext.SubtypeChoice ctx = (ChoiceContext.SubtypeChoice) gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context();
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

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.options()).containsExactly("ARTIFACT", "CREATURE", "ENCHANTMENT", "LAND", "PLANESWALKER");
            assertThat(msg.prompt()).isEqualTo("Choose a permanent type.");
        }

        @Test
        @DisplayName("Stores PermanentTypeChoice context with destination and description")
        void storesContext() {
            svc.beginPermanentTypeChoice(gd, PLAYER1_ID, GraveyardChoiceDestination.HAND, "entry desc");

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context()).isInstanceOf(ChoiceContext.PermanentTypeChoice.class);
            ChoiceContext.PermanentTypeChoice ctx = (ChoiceContext.PermanentTypeChoice) gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context();
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

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.options()).containsExactly("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");
            assertThat(msg.prompt()).isEqualTo("Choose a basic land type.");
        }

        @Test
        @DisplayName("Stores BasicLandTypeChoice context")
        void storesContext() {
            UUID permId = UUID.randomUUID();

            svc.beginBasicLandTypeChoice(gd, PLAYER1_ID, permId);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context()).isInstanceOf(ChoiceContext.BasicLandTypeChoice.class);
            ChoiceContext.BasicLandTypeChoice ctx = (ChoiceContext.BasicLandTypeChoice) gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context();
            assertThat(ctx.permanentId()).isEqualTo(permId);
        }

        @Test
        @DisplayName("Offers only the allowed types when the choice is restricted")
        void sendsOnlyAllowedTypes() {
            UUID permId = UUID.randomUUID();

            svc.beginBasicLandTypeChoice(gd, PLAYER1_ID, permId, false, false,
                    List.of(CardSubtype.ISLAND, CardSubtype.SWAMP));

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.options()).containsExactly("ISLAND", "SWAMP");

            ChoiceContext.BasicLandTypeChoice ctx =
                    (ChoiceContext.BasicLandTypeChoice) gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context();
            assertThat(ctx.allowedTypes()).containsExactly(CardSubtype.ISLAND, CardSubtype.SWAMP);
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
            gd.addToExile(PLAYER1_ID, exiledCard);

            svc.beginCardNameChoice(gd, PLAYER1_ID, sourceCard, List.of());

            InteractionPromptMessage msg = projectedPrompt();
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

            InteractionPromptMessage msg = projectedPrompt();
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

            InteractionPromptMessage msg = projectedPrompt();
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

            InteractionPromptMessage msg = projectedPrompt();
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

            InteractionPromptMessage msg = projectedPrompt();
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

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context()).isInstanceOf(ChoiceContext.CardNameChoice.class);
            ChoiceContext.CardNameChoice ctx = (ChoiceContext.CardNameChoice) gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context();
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
        @DisplayName("Stores choosingPlayerId as the decision owner")
        void storesChoosingPlayer() {
            Card card = createCreature("Bear");
            gd.playerHands.get(PLAYER2_ID).add(card);

            svc.beginSpellCardNameChoice(gd, PLAYER1_ID, PLAYER2_ID, List.of(CardType.LAND), null);

            assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(PLAYER1_ID);
        }

        @Test
        @DisplayName("Stores ExileByNameChoice context")
        void storesContext() {
            svc.beginSpellCardNameChoice(gd, PLAYER1_ID, PLAYER2_ID, List.of(CardType.LAND), null);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context()).isInstanceOf(ChoiceContext.ExileByNameChoice.class);
            ChoiceContext.ExileByNameChoice ctx = (ChoiceContext.ExileByNameChoice) gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context();
            assertThat(ctx.targetPlayerId()).isEqualTo(PLAYER2_ID);
            assertThat(ctx.controllerId()).isEqualTo(PLAYER1_ID);
        }

        @Test
        @DisplayName("Stores a finite exile cap and hand-exile draw rider")
        void storesFiniteExileOptions() {
            svc.beginSpellCardNameChoice(gd, PLAYER1_ID, PLAYER2_ID, List.of(), null, 4, true);

            ChoiceContext.ExileByNameChoice ctx =
                    (ChoiceContext.ExileByNameChoice) gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context();
            assertThat(ctx.maxCount()).isEqualTo(4);
            assertThat(ctx.drawForEachHandCardExiled()).isTrue();
        }

        @Test
        @DisplayName("A required type offers only names of cards with that type")
        void requiredTypeOffersOnlyMatchingNames() {
            Card artifact = createCard("Sol Ring", CardType.ARTIFACT);
            Card creature = createCreature("Bear");
            gd.playerHands.get(PLAYER2_ID).add(artifact);
            gd.playerHands.get(PLAYER2_ID).add(creature);

            svc.beginSpellCardNameChoice(gd, PLAYER1_ID, PLAYER2_ID, List.of(), CardType.ARTIFACT);

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.options()).contains("Sol Ring");
            assertThat(msg.options()).doesNotContain("Bear");
        }

        @Test
        @DisplayName("Excludes basic land names while retaining nonbasic land names")
        void excludesBasicLandNames() {
            Card basicLand = createCard("Plains", CardType.LAND);
            basicLand.setSupertypes(Set.of(CardSupertype.BASIC));
            Card nonbasicLand = createCard("Bojuka Bog", CardType.LAND);
            Card creature = createCreature("Bear");
            gd.playerHands.get(PLAYER2_ID).addAll(List.of(basicLand, nonbasicLand, creature));
            CreateTokenEffect tokenTemplate = CreateTokenEffect.blackZombie(1);

            svc.beginSpellCardNameChoice(gd, PLAYER1_ID, PLAYER2_ID, List.of(), null,
                    true, tokenTemplate, "M21");

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.options()).contains("Bojuka Bog", "Bear");
            assertThat(msg.options()).doesNotContain("Plains");
            assertThat(msg.prompt()).isEqualTo("Choose a card name other than a basic land card name.");
            ChoiceContext.ExileByNameChoice ctx = (ChoiceContext.ExileByNameChoice)
                    gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context();
            assertThat(ctx.tokenTemplate()).isEqualTo(tokenTemplate);
            assertThat(ctx.sourceSetCode()).isEqualTo("M21");
        }
    }

    // ========================================================================
    // beginSphinxAmbassadorCardNameChoice
    // ========================================================================

    @Nested
    @DisplayName("beginSphinxAmbassadorCardNameChoice")
    class BeginSphinxAmbassadorCardNameChoice {

        @Test
        @DisplayName("Collects all card names for the naming player")
        void collectsAllNames() {
            Card card = createCreature("Sphinx");
            gd.playerHands.get(PLAYER2_ID).add(card);

            svc.beginSphinxAmbassadorCardNameChoice(gd, PLAYER2_ID, PLAYER1_ID);

            assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(PLAYER2_ID);
            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.options()).contains("Sphinx");
            assertThat(msg.prompt()).isEqualTo("Choose a card name.");
        }

        @Test
        @DisplayName("Stores SphinxAmbassadorNameChoice context")
        void storesContext() {
            svc.beginSphinxAmbassadorCardNameChoice(gd, PLAYER2_ID, PLAYER1_ID);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context()).isInstanceOf(ChoiceContext.SphinxAmbassadorNameChoice.class);
            ChoiceContext.SphinxAmbassadorNameChoice ctx = (ChoiceContext.SphinxAmbassadorNameChoice) gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context();
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
            gd.playerHands.get(PLAYER2_ID).add(card);

            svc.beginMultiZoneExileChoice(gd, PLAYER1_ID, List.of(card), PLAYER2_ID, "Bear");

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
        }

        @Test
        @DisplayName("Sends correct message with card views")
        void sendsMessage() {
            Card card1 = createCreature("Bear");
            Card card2 = createCreature("Bear");
            gd.playerHands.get(PLAYER2_ID).add(card1);
            gd.playerGraveyards.get(PLAYER2_ID).add(card2);
            CardView view1 = mock(CardView.class);
            CardView view2 = mock(CardView.class);
            when(cardViewFactory.create(card1)).thenReturn(view1);
            when(cardViewFactory.create(card2)).thenReturn(view2);

            svc.beginMultiZoneExileChoice(gd, PLAYER1_ID, List.of(card1, card2), PLAYER2_ID, "Bear");

            InteractionPromptMessage msg = projectedPrompt();
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

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ImprintFromHandChoice.class);
        }

        @Test
        @DisplayName("Sends InteractionPromptMessage without canDecline")
        void sendsMessage() {
            UUID sourcePermId = UUID.randomUUID();

            svc.beginImprintFromHandChoice(gd, PLAYER1_ID, List.of(0, 2), "Choose to imprint", sourcePermId);

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.cardIndices()).containsExactly(0, 2);
            assertThat(msg.prompt()).isEqualTo("Choose to imprint");
            assertThat(msg.declinable()).isFalse();
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

            svc.beginExileFromHandChoice(gd, PLAYER1_ID, sourcePermId, 1);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        }

        @Test
        @DisplayName("Generates valid indices for all cards in hand")
        void generatesValidIndicesForEntireHand() {
            gd.playerHands.get(PLAYER1_ID).addAll(List.of(createCreature("A"), createCreature("B"), createCreature("C")));
            UUID sourcePermId = UUID.randomUUID();

            svc.beginExileFromHandChoice(gd, PLAYER1_ID, sourcePermId, 1);

            InteractionPromptMessage msg = projectedPrompt();
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

            svc.beginDiscardChoice(gd, PLAYER1_ID, 1);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.cardIndices()).containsExactly(0, 1);
            assertThat(msg.prompt()).isEqualTo("Choose a card to discard.");
        }

        @Test
        @DisplayName("Parameterized version uses provided indices and prompt")
        void parameterizedVersionUsesProvidedArgs() {
            svc.beginDiscardChoice(gd, PLAYER1_ID, List.of(1, 3), "Discard a land", 1);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.cardIndices()).containsExactly(1, 3);
            assertThat(msg.prompt()).isEqualTo("Discard a land");
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

            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("Clears pending abilities and does nothing when game is FINISHED")
        void clearsAndDoesNothingWhenFinished() {
            gd.status = GameStatus.FINISHED;
            Card card = createCreature("Source");
            gd.pendingMayAbilities.add(new PendingMayAbility(card, PLAYER1_ID, List.of(), "May draw a card"));

            svc.processNextMayAbility(gd);

            assertThat(gd.pendingMayAbilities).isEmpty();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("Sends InteractionPromptMessage for first pending ability without mana cost")
        void sendsMessageForAbilityWithoutManaCost() {
            Card card = createCreature("Source");
            gd.pendingMayAbilities.add(new PendingMayAbility(card, PLAYER1_ID, List.of(), "May draw a card"));

            svc.processNextMayAbility(gd);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            InteractionPromptMessage msg = projectedPrompt();
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

            InteractionPromptMessage msg = projectedPrompt();
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

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.canPay()).isFalse();
        }

        @Test
        @DisplayName("Reports canPay=true for X cost when player has any mana")
        void reportsCanPayForXCostWithMana() {
            Card card = createCreature("Source");
            gd.pendingMayAbilities.add(new PendingMayAbility(card, PLAYER1_ID, List.of(), "Pay {X}", null, "{X}"));
            gd.playerManaPools.get(PLAYER1_ID).add(ManaColor.RED, 1);

            svc.processNextMayAbility(gd);

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.canPay()).isTrue();
        }

        @Test
        @DisplayName("Reports canPay=false for X cost when player has no mana")
        void reportsCanPayFalseForXCostWithNoMana() {
            Card card = createCreature("Source");
            gd.pendingMayAbilities.add(new PendingMayAbility(card, PLAYER1_ID, List.of(), "Pay {X}", null, "{X}"));

            svc.processNextMayAbility(gd);

            InteractionPromptMessage msg = projectedPrompt();
            assertThat(msg.canPay()).isFalse();
        }

        @Test
        @DisplayName("Keeps the affected player as decision owner when mind-controlled")
        void keepsAffectedPlayerAsDecisionOwnerWhenMindControlled() {
            UUID controllerId = UUID.randomUUID();
            gd.playerIdToName.put(controllerId, "Controller");
            gd.mindControlledPlayerId = PLAYER1_ID;
            gd.mindControllerPlayerId = controllerId;

            Card card = createCreature("Source");
            gd.pendingMayAbilities.add(new PendingMayAbility(card, PLAYER1_ID, List.of(), "May do something"));

            svc.processNextMayAbility(gd);

            assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(PLAYER1_ID);
            assertThat(projectedPrompt()).isNotNull();
        }
    }
}
