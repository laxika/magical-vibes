package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LluwenImperfectNaturalist.class, Forest.class, Mountain.class, GrizzlyBears.class, GiantGrowth.class})
class LluwenImperfectNaturalistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills four and may put a milled creature or land on top")
    void etbMillsAndMayPutEligibleCardOnTop() {
        GrizzlyBears creature = new GrizzlyBears();
        Forest land = new Forest();
        Mountain otherLand = new Mountain();
        harness.setLibrary(player1, List.of(creature, land, otherLand, new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LluwenImperfectNaturalist()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("ETB only offers creature or land cards from the milled cards")
    void etbOnlyOffersCreatureOrLandCards() {
        GiantGrowth instant = new GiantGrowth();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(instant, land, new Mountain(), new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LluwenImperfectNaturalist()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant);
    }

    @Test
    @DisplayName("Discarding a land creates one Worm token per land in the graveyard")
    void activationCreatesWormsForLandsInGraveyard() {
        Permanent lluwen = addCreatureReady(player1, new LluwenImperfectNaturalist());
        harness.setHand(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(new Mountain(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        long wormCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
        assertThat(wormCount).isEqualTo(3);
    }

    @Test
    void mayDeclineEveryEligibleCard() {
        Forest land = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        Mountain remaining = new Mountain();
        harness.setLibrary(player1, List.of(land, creature, new GiantGrowth(), new GiantGrowth(), remaining));
        castLluwenAndResolveTrigger();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land, creature).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayChooseLaterMilledCardAndReturnOnlyOne() {
        Forest first = new Forest();
        GrizzlyBears chosen = new GrizzlyBears();
        Mountain third = new Mountain();
        GiantGrowth remaining = new GiantGrowth();
        harness.setLibrary(player1, List.of(first, chosen, third, new Forest(), remaining));
        castLluwenAndResolveTrigger();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen, remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, third).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void millsShortLibraryAndDoesNotOfferPreexistingGraveyardCards() {
        Forest oldLand = new Forest();
        GiantGrowth first = new GiantGrowth();
        GiantGrowth second = new GiantGrowth();
        harness.setGraveyard(player1, List.of(oldLand));
        harness.setLibrary(player1, List.of(first, second));
        castLluwenAndResolveTrigger();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(oldLand, first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void activationCountsOnlyControllersLandsAtResolutionAndSurvivesSourceLeaving() {
        Permanent lluwen = addCreatureReady(player1, new LluwenImperfectNaturalist());
        Forest discarded = new Forest();
        harness.setHand(player1, List.of(new GiantGrowth(), discarded));
        harness.setGraveyard(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Forest(), new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.DiscardCostChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
        harness.handleCardChosen(player1, 1);
        assertThat(lluwen.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());

        harness.setGraveyard(player1, List.of(new Mountain(), new GrizzlyBears()));
        gd.playerBattlefields.get(player1.getId()).remove(lluwen);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        var token = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        assertThat(token.isToken()).isTrue();
        assertThat(token.getPower()).isEqualTo(1);
        assertThat(token.getToughness()).isEqualTo(1);
        assertThat(token.getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(token.getSubtypes()).containsExactly(CardSubtype.WORM);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void createsNoWormsWhenNoLandsRemainAtResolution() {
        Permanent lluwen = addCreatureReady(player1, new LluwenImperfectNaturalist());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(lluwen);
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent lluwen = harness.addToBattlefieldAndReturn(player1, new LluwenImperfectNaturalist());
        lluwen.setSummoningSick(true);
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(lluwen.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateWithoutLandToDiscard() {
        Permanent lluwen = addCreatureReady(player1, new LluwenImperfectNaturalist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(lluwen.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void castLluwenAndResolveTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LluwenImperfectNaturalist()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
