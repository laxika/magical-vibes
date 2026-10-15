package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GargantuanSlabhorn;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoyalCatharUnhallowedCathar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forest.class, GargantuanSlabhorn.class, GrizzlyBears.class,
        InvasionOfPyrulea.class, LoyalCatharUnhallowedCathar.class, Shock.class})
class InvasionOfPyruleaTest extends BaseCardTest {

    @Test
    @DisplayName("Scrying to a land makes the Siege draw it")
    void scriesThenDrawsLand() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        castInvasion();
        finishScryKeepingAllCardsOnTop();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("A double-faced top card also makes the Siege draw")
    void scriesThenDrawsDoubleFacedCard() {
        InvasionOfPyrulea doubleFacedCard = new InvasionOfPyrulea();
        harness.setLibrary(player1, List.of(doubleFacedCard, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        castInvasion();
        finishScryKeepingAllCardsOnTop();

        assertThat(gd.playerHands.get(player1.getId())).contains(doubleFacedCard);
    }

    @Test
    @DisplayName("A nonland, nondouble-faced top card is not drawn")
    void doesNotDrawNonMatchingTopCard() {
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        castInvasion();
        finishScryKeepingAllCardsOnTop();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Defeating the Siege casts Gargantuan Slabhorn transformed")
    void defeatingTheSiegeCastsTheBackFaceTransformed() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfPyrulea());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent slabhorn = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GargantuanSlabhorn)
                .findFirst()
                .orElseThrow();
        assertThat(slabhorn.isTransformed()).isTrue();
        assertThat(gqs.hasKeyword(gd, slabhorn, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Gargantuan Slabhorn gives other transformed permanents trample and ward")
    void grantsTrampleAndWardToOtherTransformedPermanents() {
        addTransformedPyrulea();
        Permanent other = addTransformedCathar();

        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, other.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(other);
    }

    @Test
    @DisplayName("Gargantuan Slabhorn has ward")
    void hasWard() {
        Permanent slabhorn = addTransformedPyrulea();

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, slabhorn.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(slabhorn);
    }

    @Test
    void drawsTheLandMovedToTheTopByScry() {
        GrizzlyBears first = new GrizzlyBears();
        Forest land = new Forest();
        GrizzlyBears third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, land, third));

        castInvasion();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0, 2), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, third);
    }

    @Test
    void revealsAndDrawsTheFourthCardWhenAllScryCardsGoToTheBottom() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, land));

        castInvasion();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1, 2)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third);
    }

    @Test
    void canScryAndDrawWithOnlyOneCardInTheLibrary() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));

        castInvasion();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotCauseADraw() {
        harness.setLibrary(player1, List.of());

        castInvasion();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Invasion of Pyrulea");
    }

    @Test
    void doesNotGrantTrampleOrWardToAnUntransformedCreature() {
        addTransformedPyrulea();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void doesNotGrantTrampleToAnOpponentsTransformedPermanent() {
        addTransformedPyrulea();
        Permanent other = harness.addToBattlefieldAndReturn(player2, new LoyalCatharUnhallowedCathar());
        other.setCard(other.getOriginalCard().getBackFaceCard());
        other.setTransformed(true);

        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void itsWardCountersShockWithoutDealingDamage() {
        Permanent slabhorn = addTransformedPyrulea();
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, slabhorn.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(slabhorn.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    private void castInvasion() {
        harness.setHand(player1, List.of(new InvasionOfPyrulea()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        gs.playCard(gd, player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void finishScryKeepingAllCardsOnTop() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));
    }

    private Permanent addTransformedPyrulea() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new InvasionOfPyrulea());
        permanent.setCard(permanent.getOriginalCard().getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private Permanent addTransformedCathar() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new LoyalCatharUnhallowedCathar());
        permanent.setCard(permanent.getOriginalCard().getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }
}
