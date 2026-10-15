package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.s.SparkSpray;
import com.github.laxika.magicalvibes.cards.t.TreetopScout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathsHeadBuzzard.class, GoblinBrigand.class, SparkSpray.class, TreetopScout.class})
class DeathsHeadBuzzardTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, all creatures get -1/-1 until end of turn")
    void deathTriggerDebuffsAllCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GoblinBrigand());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        Permanent buzzard = harness.addToBattlefieldAndReturn(player1, new DeathsHeadBuzzard());

        destroyBuzzard(buzzard);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(1);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The debuff destroys one-toughness creatures and does not affect later creatures")
    void deathTriggerHandlesZeroToughnessAndLaterEntrants() {
        Permanent sturdyCreature = harness.addToBattlefieldAndReturn(player1, new GoblinBrigand());
        harness.addToBattlefieldAndReturn(player1, new TreetopScout());
        harness.addToBattlefieldAndReturn(player2, new TreetopScout());
        Permanent buzzard = harness.addToBattlefieldAndReturn(player1, new DeathsHeadBuzzard());

        destroyBuzzard(buzzard);

        assertThat(sturdyCreature.getEffectivePower()).isEqualTo(1);
        assertThat(sturdyCreature.getEffectiveToughness()).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Treetop Scout");
        harness.assertNotOnBattlefield(player2, "Treetop Scout");
        harness.assertInGraveyard(player1, "Treetop Scout");
        harness.assertInGraveyard(player2, "Treetop Scout");

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GoblinBrigand());
        assertThat(laterCreature.getEffectivePower()).isEqualTo(2);
        assertThat(laterCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The death trigger's debuff expires at end of turn")
    void deathTriggerDebuffExpiresAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GoblinBrigand());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        Permanent buzzard = harness.addToBattlefieldAndReturn(player1, new DeathsHeadBuzzard());

        destroyBuzzard(buzzard);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering before the death trigger resolves are affected")
    void deathTriggerAffectsCreaturesPresentAtResolution() {
        Permanent buzzard = harness.addToBattlefieldAndReturn(player1, new DeathsHeadBuzzard());
        harness.setHand(player1, java.util.List.of(new SparkSpray()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, buzzard.getId());

        harness.assertInGraveyard(player1, "Death's-Head Buzzard");
        Permanent entrant = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        assertThat(entrant.getEffectiveToughness()).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(entrant.getEffectivePower()).isEqualTo(1);
        assertThat(entrant.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Buzzard killed by another Buzzard's trigger creates its own death trigger")
    void deathTriggersChainAndDebuffsAccumulate() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GoblinBrigand());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        Permanent buzzard = harness.addToBattlefieldAndReturn(player1, new DeathsHeadBuzzard());
        harness.addToBattlefield(player2, new DeathsHeadBuzzard());

        destroyBuzzard(buzzard);

        harness.assertInGraveyard(player1, "Death's-Head Buzzard");
        harness.assertInGraveyard(player2, "Death's-Head Buzzard");
        harness.assertNotOnBattlefield(player2, "Death's-Head Buzzard");
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Brigand");
        harness.assertNotOnBattlefield(player2, "Goblin Brigand");
        harness.assertInGraveyard(player1, "Goblin Brigand");
        harness.assertInGraveyard(player2, "Goblin Brigand");
    }

    private void destroyBuzzard(Permanent buzzard) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, java.util.List.of(new SparkSpray()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, buzzard.getId());
        harness.passBothPriorities();
    }
}
