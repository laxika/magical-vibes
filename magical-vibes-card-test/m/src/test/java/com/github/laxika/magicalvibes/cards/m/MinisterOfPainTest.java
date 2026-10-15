package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DromokaWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinisterOfPain.class, GrizzlyBears.class, DromokaWarrior.class})
class MinisterOfPainTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit does not debuff opposing creatures")
    void decliningExploitDoesNothing() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castMinisterToExploitPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exploiting a creature gives opposing creatures -1/-1 until end of turn")
    void exploitDebuffsOpposingCreaturesOnly() {
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castMinisterToExploitPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("The exploit debuff wears off at end of turn")
    void exploitDebuffWearsOffAtEndOfTurn() {
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castMinisterToExploitPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Minister can exploit itself and still debuff opposing creatures")
    void exploitingItselfTriggersDebuff() {
        Permanent opponentCreature = addCreatureReady(player2, new MinisterOfPain());

        castMinisterToExploitPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Minister of Pain"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Minister of Pain");
        harness.assertInGraveyard(player1, "Minister of Pain");
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The debuff kills opposing creatures with one toughness")
    void debuffKillsOneToughnessCreatures() {
        addCreatureReady(player2, new DromokaWarrior());
        Permanent ownCreature = addCreatureReady(player1, new DromokaWarrior());

        castMinisterToExploitPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Minister of Pain"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dromoka Warrior");
        harness.assertInGraveyard(player2, "Dromoka Warrior");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures entering after the debuff resolves are unaffected")
    void laterCreaturesAreNotDebuffed() {
        Permanent existingCreature = addCreatureReady(player2, new MinisterOfPain());

        castMinisterToExploitPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Minister of Pain"));
        harness.passBothPriorities();

        Permanent laterCreature = harness.enterBattlefieldAndReturn(player2, new DromokaWarrior());

        assertThat(gqs.getEffectivePower(gd, existingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, existingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(laterCreature);
    }

    private void castMinisterToExploitPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MinisterOfPain(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
