package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LochLarent.class, Forest.class, GrizzlyBears.class})
class LochLarentTest extends BaseCardTest {

    @Test
    @DisplayName("Loch Larent enters tapped and taps for blue mana")
    void entersTappedAndTapsForBlueMana() {
        harness.setHand(player1, List.of(new LochLarent()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        Permanent land = findPermanent(player1, "Loch Larent");
        assertThat(land.isTapped()).isTrue();

        land.enterUntapped();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loch Larent's one-time boon makes the next opponent creature enter tapped with a stun counter")
    void oneTimeBoonModifiesNextOpponentCreatureSpell() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new LochLarent());
        land.enterUntapped();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));

        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent firstBear = findPermanents(player2, "Grizzly Bears").getFirst();
        assertThat(firstBear.isTapped()).isTrue();
        assertThat(firstBear.getCounterCount(CounterType.STUN)).isEqualTo(1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent secondBear = findPermanents(player2, "Grizzly Bears").get(1);
        assertThat(secondBear.isTapped()).isFalse();
        assertThat(secondBear.getCounterCount(CounterType.STUN)).isZero();
    }
    @Test
    void boonPersistsUntilOpponentCastsCreatureOnALaterTurn() {
        resolveBoon();
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        Permanent bear = findPermanent(player2, "Grizzly Bears");
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void cannotActivateBoonTwiceEvenAfterUntapping() {
        Permanent land = resolveBoon();
        land.enterUntapped();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    void cannotActivateBoonDuringOpponentsTurn() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new LochLarent());
        land.enterUntapped();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void scryCanReorderTopCardsAndPutCardsOnBottom() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new LochLarent());
        land.enterUntapped();
        Forest first = new Forest();
        GrizzlyBears second = new GrizzlyBears();
        LochLarent third = new LochLarent();
        Forest fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(2, 0), List.of(1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, first, fourth, second);
    }

    @Test
    void boonCannotTargetItsController() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new LochLarent());
        land.enterUntapped();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void boonSurvivesSourceLeavingAndIgnoresControllersCreature() {
        Permanent land = resolveBoon();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent ownBear = findPermanent(player1, "Grizzly Bears");
        assertThat(ownBear.isTapped()).isFalse();
        assertThat(ownBear.getCounterCount(CounterType.STUN)).isZero();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent opposingBear = findPermanent(player2, "Grizzly Bears");
        assertThat(opposingBear.isTapped()).isTrue();
        assertThat(opposingBear.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }
    private Permanent resolveBoon() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new LochLarent());
        land.enterUntapped();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));
        return land;
    }
}
