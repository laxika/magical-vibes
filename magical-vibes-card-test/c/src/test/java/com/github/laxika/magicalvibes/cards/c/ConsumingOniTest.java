package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConsumingOni.class, Forest.class, GrizzlyBears.class})
class ConsumingOniTest extends BaseCardTest {

    @Test
    void endStepMarksOneRandomNonlandCardInHand() {
        addCreatureReady(player1, new ConsumingOni());
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), forest));
        advanceThroughEndStep(player1);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void repeatedGrantsCreateIndependentCastTriggers() {
        addCreatureReady(player1, new ConsumingOni());
        harness.setHand(player1, List.of(new ConsumingOni()));

        advanceThroughEndStep(player1);
        advanceThroughEndStep(player1);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY))
                .hasSize(2);
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void doesNotGrantAbilityDuringOpponentsEndStep() {
        addCreatureReady(player1, new ConsumingOni());
        harness.setHand(player1, List.of(new ConsumingOni()));

        advanceThroughEndStep(player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void doesNotModifyOpponentsHand() {
        addCreatureReady(player1, new ConsumingOni());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new ConsumingOni()));

        advanceThroughEndStep(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void landOnlyHandLeavesHandAndLifeUnchanged() {
        addCreatureReady(player1, new ConsumingOni());
        Forest forest = new Forest();
        harness.setHand(player1, List.of(forest));

        advanceThroughEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void grantingAbilityDoesNotLoseLifeUntilMarkedSpellIsCast() {
        addCreatureReady(player1, new ConsumingOni());
        harness.setHand(player1, List.of(new ConsumingOni()));

        advanceThroughEndStep(player1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void advanceThroughEndStep(Player activePlayer) {
        gd.turnNumber++;
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);
    }

    @Test
    @CardUsed({Unsummon.class})
    void marksNoncreatureSpellsAndGrantSurvivesSourceLeavingBattlefield() {
        var oni = addCreatureReady(player1, new ConsumingOni());
        harness.setHand(player1, List.of(new Unsummon()));

        advanceThroughEndStep(player1);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, oni.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({Unsummon.class})
    void markedCardKeepsCastPenaltyAfterReturningToHand() {
        addCreatureReady(player1, new ConsumingOni());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));

        advanceThroughEndStep(player1);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Grizzly Bears").getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }
}
