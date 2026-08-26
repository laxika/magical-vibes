package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingAbilityActivation;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpecializedInteractionAiStrategiesTest {

    private UUID aiPlayerId;
    private GameData gameData;
    private AiGameActions actions;
    private AiInteractionContext context;

    @BeforeEach
    void setUp() {
        aiPlayerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "specialized-ai", aiPlayerId, "AI");
        gameData.playerHands.put(aiPlayerId, new ArrayList<>());
        gameData.playerGraveyards.put(aiPlayerId, new ArrayList<>());
        gameData.playerDecks.put(aiPlayerId, new ArrayList<>());
        gameData.playerSideboards.put(aiPlayerId, new ArrayList<>());
        actions = mock(AiGameActions.class);
        context = new AiInteractionContext(
                gameData,
                gameData.id,
                aiPlayerId,
                mock(GameQueryService.class),
                actions);
    }

    @Test
    void adNauseamDeclinesWhenTheNextCardWouldBeLethal() throws Exception {
        gameData.playerLifeTotals.put(aiPlayerId, 3);
        gameData.playerDecks.get(aiPlayerId).add(card("Lethal", "{3}"));

        new AdNauseamRepeatChoiceAiStrategy().answer(
                new PendingInteraction.AdNauseamRepeatChoice(aiPlayerId, "Ad Nauseam"), context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.MayAbilityChosen(false));
    }

    @Test
    void keepCardsInHandChoosesOnlyTheHighestValueLegalCards() throws Exception {
        Card cheap = card("Cheap", "{1}");
        Card expensive = card("Expensive", "{5}");
        Card invalid = card("Invalid", "{9}");
        gameData.playerHands.get(aiPlayerId).addAll(List.of(cheap, expensive, invalid));

        new KeepCardsInHandChoiceAiStrategy().answer(
                new PendingInteraction.KeepCardsInHandChoice(
                        aiPlayerId,
                        List.of(cheap.getId(), expensive.getId()),
                        1,
                        List.of(),
                        "Source"),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of(expensive.getId())));
    }

    @Test
    void sylvanLibraryPutsBackEligibleCardsInsteadOfPayingLife() throws Exception {
        Card first = card("First", "{1}");
        Card second = card("Second", "{2}");
        gameData.playerHands.get(aiPlayerId).addAll(List.of(first, second));

        new SylvanLibraryChoiceAiStrategy().answer(
                new PendingInteraction.SylvanLibraryChoice(
                        aiPlayerId, List.of(first.getId(), second.getId()), 2),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(
                        List.of(first.getId(), second.getId())));
    }

    @Test
    void libraryRevealRespectsZeroMaximum() throws Exception {
        UUID validCardId = UUID.randomUUID();

        new LibraryRevealChoiceAiStrategy().answer(
                new PendingInteraction.LibraryRevealChoice(
                        aiPlayerId, List.of(), List.of(validCardId), false, false,
                        false, false, false, 0, null, 0, "Choose up to zero cards."),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of()));
    }

    @Test
    void multiZoneExileRespectsMaximum() throws Exception {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID third = UUID.randomUUID();

        new MultiZoneExileChoiceAiStrategy().answer(
                new PendingInteraction.MultiZoneExileChoice(
                        aiPlayerId, List.of(first, second, third), 2,
                        UUID.randomUUID(), aiPlayerId, "Plains"),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of(first, second)));
    }

    @Test
    void brilliantUltimatumPileStrategiesProduceLegalAnswers() throws Exception {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID third = UUID.randomUUID();

        new BrilliantUltimatumPileSeparationChoiceAiStrategy().answer(
                new PendingInteraction.BrilliantUltimatumPileSeparationChoice(
                        aiPlayerId, List.of(first, second, third)),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of(first)));
    }

    @Test
    void activatedAbilityGraveyardExileCostChoosesMaximumX() throws Exception {
        Card first = card("First", "{1}");
        Card second = card("Second", "{2}");

        new ActivatedAbilityGraveyardExileCostChoiceAiStrategy().answer(
                new PendingInteraction.ActivatedAbilityGraveyardExileCostChoice(
                        aiPlayerId, UUID.randomUUID(), 0, UUID.randomUUID(), null,
                        List.of(first, second), "Choose cards to exile.", 0, 2, false),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of(first.getId(), second.getId())));
    }

    @Test
    void craftMaterialChoiceChoosesTheRequiredCards() throws Exception {
        Card first = card("First", "{1}");
        Card second = card("Second", "{2}");

        new CraftMaterialChoiceAiStrategy().answer(
                new PendingInteraction.CraftMaterialChoice(
                        aiPlayerId, UUID.randomUUID(), 0, 0, null, null, List.of(),
                        java.util.Map.of(), List.of(first, second), 1, 1,
                        "Choose an artifact to exile."),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of(first.getId())));
    }

    @Test
    void graveyardExileCostChoosesAnAffordableCardWhenTheAbilityPaysItsManaCost() throws Exception {
        UUID sourceId = UUID.randomUUID();
        Permanent source = mock(Permanent.class);
        when(source.getId()).thenReturn(sourceId);
        ActivatedAbility ability = new ActivatedAbility(
                false,
                null,
                List.of(new ExileCardFromGraveyardCost(CardType.CREATURE, true, false)),
                "Choose a creature and pay its mana cost.");
        when(context.gameQueryService().findPermanentById(gameData, sourceId)).thenReturn(source);
        when(actions.getEffectiveActivatedAbilities(gameData, source)).thenReturn(List.of(ability));

        Card affordable = card("Affordable", "{1}");
        affordable.setType(CardType.CREATURE);
        Card expensive = card("Expensive", "{5}");
        expensive.setType(CardType.CREATURE);
        gameData.playerGraveyards.put(aiPlayerId, new ArrayList<>(List.of(affordable, expensive)));
        ManaPool manaPool = new ManaPool();
        manaPool.add(ManaColor.COLORLESS);
        gameData.playerManaPools.put(aiPlayerId, manaPool);
        gameData.pendingAbilityActivation = new PendingAbilityActivation(
                sourceId, 0, 0, null, null, null);

        new GraveyardExileCostChoiceAiStrategy().answer(
                new PendingInteraction.GraveyardExileCostChoice(
                        aiPlayerId, List.of(0, 1), "Choose a creature to exile."),
                context);

        assertThat(capturedAnswer()).isEqualTo(new InteractionAnswer.GraveyardCardChosen(0));
    }

    @Test
    void xValueChoiceReservesFixedManaComponents() throws Exception {
        ManaPool manaPool = new ManaPool();
        manaPool.add(ManaColor.RED, 4);
        gameData.playerManaPools.put(aiPlayerId, manaPool);

        new XValueChoiceAiStrategy().answer(
                new PendingInteraction.XValueChoice(
                        aiPlayerId, 7, "Choose X.", "Flameblast Dragon", true, "{X}{R}"),
                context);

        assertThat(capturedAnswer()).isEqualTo(new InteractionAnswer.NumberChosen(3));
    }

    @Test
    void xValueChoiceUsesXCostOnlyMana() throws Exception {
        ManaPool manaPool = new ManaPool();
        manaPool.addXCostOnlyColorless(4);
        gameData.playerManaPools.put(aiPlayerId, manaPool);

        new XValueChoiceAiStrategy().answer(
                new PendingInteraction.XValueChoice(
                        aiPlayerId, 7, "Choose X.", "Vigil for the Lost", true),
                context);

        assertThat(capturedAnswer()).isEqualTo(new InteractionAnswer.NumberChosen(4));
    }

    @Test
    void turnFaceUpXValueChoiceReservesFixedManaComponents() throws Exception {
        ManaPool manaPool = new ManaPool();
        manaPool.add(ManaColor.WHITE, 4);
        gameData.playerManaPools.put(aiPlayerId, manaPool);

        new TurnFaceUpXValueChoiceAiStrategy().answer(
                new PendingInteraction.TurnFaceUpXValueChoice(
                        aiPlayerId, UUID.randomUUID(), "{X}{W}", 7,
                        "Choose X.", "Aurelia's Vindicator"),
                context);

        assertThat(capturedAnswer()).isEqualTo(new InteractionAnswer.NumberChosen(3));
    }

    @Test
    void exiledCreatureCopyChoiceChoosesAnEligibleCard() throws Exception {
        UUID first = UUID.randomUUID();

        new ExiledCreatureCopyChoiceAiStrategy().answer(
                new PendingInteraction.ExiledCreatureCopyChoice(
                        aiPlayerId, UUID.randomUUID(), List.of(first, UUID.randomUUID())),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of(first)));
    }

    @Test
    void exileFromOpponentHandOrGraveyardChoosesHighestManaValue() throws Exception {
        UUID opponentId = UUID.randomUUID();
        Card handCard = card("Hand card", "{2}");
        Card graveyardCard = card("Graveyard card", "{5}");
        gameData.playerHands.put(opponentId, new ArrayList<>(List.of(handCard)));
        gameData.playerGraveyards.put(opponentId, new ArrayList<>(List.of(graveyardCard)));

        new ExileNonlandCardFromTargetHandOrGraveyardChoiceAiStrategy().answer(
                new PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice(
                        aiPlayerId, opponentId, List.of(handCard.getId(), graveyardCard.getId())),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of(graveyardCard.getId())));
    }

    @Test
    void exiledCardMayPlayChoosesTheFirstEligibleCard() throws Exception {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        new ExiledCardMayPlayChoiceAiStrategy().answer(
                new PendingInteraction.ExiledCardMayPlayChoice(aiPlayerId, List.of(first, second)),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of(first)));
    }

    @Test
    void outsideGameSearchChoosesHighestManaValueEligibleCard() throws Exception {
        Card cheap = card("Cheap", "{1}");
        Card expensive = card("Expensive", "{5}");
        Card invalid = card("Invalid", "{9}");
        gameData.playerSideboards.get(aiPlayerId).addAll(List.of(cheap, expensive, invalid));

        new SearchOutsideGameOrExileCardChoiceAiStrategy().answer(
                new PendingInteraction.SearchOutsideGameOrExileCardChoice(
                        aiPlayerId, List.of(cheap.getId(), expensive.getId()), null, "card"),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of(expensive.getId())));
    }

    @Test
    void libraryOrGraveyardSearchChoosesHighestValueCard() throws Exception {
        Card land = card("Land", "");
        land.setType(CardType.LAND);
        Card cheap = card("Cheap", "{1}");
        Card expensive = card("Expensive", "{5}");

        new SearchLibraryAndOrGraveyardChoiceAiStrategy().answer(
                new PendingInteraction.SearchLibraryAndOrGraveyardChoice(
                        aiPlayerId, List.of(land, cheap, expensive), Set.of(land.getId(), cheap.getId()),
                        true, "card"),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of(expensive.getId())));
    }

    @Test
    void targetHandSpellCopyChoosesHighestManaValueEligibleCard() throws Exception {
        UUID opponentId = UUID.randomUUID();
        Card cheap = card("Cheap", "{1}");
        Card expensive = card("Expensive", "{5}");
        Card invalid = card("Invalid", "{9}");

        new TargetHandSpellCopyChoiceAiStrategy().answer(
                new PendingInteraction.TargetHandSpellCopyChoice(
                        aiPlayerId, opponentId, List.of(cheap, expensive, invalid),
                        List.of(cheap.getId(), expensive.getId())),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of(expensive.getId())));
    }

    @Test
    void targetedHandBattlefieldChoiceChoosesHighestManaValueEligibleCard() throws Exception {
        UUID opponentId = UUID.randomUUID();
        Card cheap = card("Cheap", "{1}");
        Card expensive = card("Expensive", "{5}");
        Card invalid = card("Invalid", "{9}");
        gameData.playerHands.put(opponentId, new ArrayList<>(List.of(cheap, expensive, invalid)));

        new TargetedHandBattlefieldChoiceAiStrategy().answer(
                new PendingInteraction.TargetedHandBattlefieldChoice(
                        aiPlayerId, opponentId, List.of(0, 1), "Choose a card.", false, false),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardIndexChosen(1));
    }

    @Test
    void magesContestPassesTheBid() throws Exception {
        new MagesContestBidChoiceAiStrategy().answer(
                new PendingInteraction.MagesContestBidChoice(
                        aiPlayerId, 3, 10, "Mages' Contest", UUID.randomUUID(), UUID.randomUUID()),
                context);

        assertThat(capturedAnswer()).isEqualTo(new InteractionAnswer.NumberChosen(0));
    }

    @Test
    void targetLibraryDestinationKeepsTheOfferedPosition() throws Exception {
        new TargetLibraryDestinationChoiceAiStrategy().answer(
                new PendingInteraction.TargetLibraryDestinationChoice(
                        aiPlayerId, UUID.randomUUID(), "Target", "Second from top"),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.ListChoiceMade("Second from top"));
    }

    @Test
    void vividCardChoiceChoosesTheFirstEligibleCard() throws Exception {
        Card first = card("First", "{1}");
        Card second = card("Second", "{2}");

        new VividCardChoiceAiStrategy().answer(
                new PendingInteraction.VividCardChoice(
                        aiPlayerId, List.of(first, second), List.of(first.getId(), second.getId()),
                        List.of(CardColor.BLUE), 0, List.of(), "Choose a card"),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of(first.getId())));
    }

    @Test
    void handOrGraveyardChoiceChoosesTheHighestManaValueCard() throws Exception {
        Card handCard = card("Hand card", "{2}");
        Card graveyardCard = card("Graveyard card", "{5}");
        gameData.playerHands.get(aiPlayerId).add(handCard);
        gameData.playerGraveyards.get(aiPlayerId).add(graveyardCard);

        new PutCardFromHandOrGraveyardChoiceAiStrategy().answer(
                new PendingInteraction.PutCardFromHandOrGraveyardChoice(
                        aiPlayerId, List.of(handCard.getId(), graveyardCard.getId()), "card", "Source"),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of(graveyardCard.getId())));
    }

    @Test
    void nivMizzetChoiceSelectsOneCardForEachColorPair() throws Exception {
        Card cheapAzorius = card("Cheap Azorius", "{2}");
        Card expensiveAzorius = card("Expensive Azorius", "{5}");
        Card gruul = card("Gruul", "{3}");
        when(context.gameQueryService().getEffectiveCardColors(gameData, cheapAzorius))
                .thenReturn(Set.of(CardColor.WHITE, CardColor.BLUE));
        when(context.gameQueryService().getEffectiveCardColors(gameData, expensiveAzorius))
                .thenReturn(Set.of(CardColor.WHITE, CardColor.BLUE));
        when(context.gameQueryService().getEffectiveCardColors(gameData, gruul))
                .thenReturn(Set.of(CardColor.RED, CardColor.GREEN));

        new NivMizzetColorPairChoiceAiStrategy().answer(
                new PendingInteraction.NivMizzetColorPairChoice(
                        aiPlayerId,
                        List.of(cheapAzorius, expensiveAzorius, gruul),
                        List.of(cheapAzorius.getId(), expensiveAzorius.getId(), gruul.getId()),
                        2,
                        "Choose cards"),
                context);

        assertThat(capturedAnswer()).isEqualTo(new InteractionAnswer.CardsChosen(
                List.of(expensiveAzorius.getId(), gruul.getId())));
    }

    @Test
    void faceUpExiledCardChoiceSelectsAnEligibleCard() throws Exception {
        UUID first = UUID.randomUUID();

        new FaceUpExiledCardChoiceAiStrategy().answer(
                new PendingInteraction.FaceUpExiledCardChoice(aiPlayerId, aiPlayerId, List.of(first)),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of(first)));
    }

    @Test
    void allNewSpecializedTypesAreRegistered() {
        assertThat(AiInteractionStrategies.registeredTypes()).contains(
                PendingInteraction.BrilliantUltimatumPileSeparationChoice.class,
                PendingInteraction.BrilliantUltimatumPileChoice.class,
                PendingInteraction.KeepCardsInHandChoice.class,
                PendingInteraction.SylvanLibraryChoice.class,
                PendingInteraction.AdNauseamRepeatChoice.class,
                PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class,
                PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice.class,
                PendingInteraction.ExiledCardMayPlayChoice.class,
                PendingInteraction.SearchOutsideGameOrExileCardChoice.class,
                PendingInteraction.TargetHandSpellCopyChoice.class,
                PendingInteraction.TargetedHandBattlefieldChoice.class,
                PendingInteraction.MagesContestBidChoice.class,
                PendingInteraction.TargetLibraryDestinationChoice.class,
                PendingInteraction.VividCardChoice.class,
                PendingInteraction.PutCardFromHandOrGraveyardChoice.class,
                PendingInteraction.NivMizzetColorPairChoice.class,
                PendingInteraction.FaceUpExiledCardChoice.class);
    }

    private InteractionAnswer capturedAnswer() throws Exception {
        ArgumentCaptor<InteractionAnswer> answer = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(actions).answerInteraction(answer.capture());
        return answer.getValue();
    }

    private static Card card(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.SORCERY);
        card.setManaCost(manaCost);
        return card;
    }
}
