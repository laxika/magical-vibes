package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodstoneGoblin.class, AcademyDrake.class, BlinkOfAnEye.class})
class BloodstoneGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a kicked creature triggers +1/+1 and menace")
    void kickedCreatureTriggersBoostAndMenace() {
        Permanent goblin = addCreatureReady(player1, new BloodstoneGoblin());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast Academy Drake with kicker: {2}{U} + kicker {4} = 7 mana
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.setHand(player1, List.of(new AcademyDrake()));

        harness.castKickedCreature(player1, 0);

        // Resolve spell cast trigger
        harness.passBothPriorities();

        assertThat(goblin.getPowerModifier()).isEqualTo(1);
        assertThat(goblin.getToughnessModifier()).isEqualTo(1);
        assertThat(goblin.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @DisplayName("Casting a non-kicked creature does not trigger")
    void nonKickedCreatureDoesNotTrigger() {
        Permanent goblin = addCreatureReady(player1, new BloodstoneGoblin());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast Academy Drake without kicker: {2}{U} = 3 mana
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player1, List.of(new AcademyDrake()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(goblin.getPowerModifier()).isEqualTo(0);
        assertThat(goblin.getToughnessModifier()).isEqualTo(0);
        assertThat(goblin.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
    }

    @Test
    @DisplayName("Casting a non-kicker creature does not trigger")
    void regularCreatureDoesNotTrigger() {
        Permanent goblin = addCreatureReady(player1, new BloodstoneGoblin());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new BloodstoneGoblin()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(goblin.getPowerModifier()).isEqualTo(0);
        assertThat(goblin.getToughnessModifier()).isEqualTo(0);
        assertThat(goblin.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
    }

    @Test
    @DisplayName("Multiple kicked spells stack the boost")
    void multipleKickedSpellsStackBoost() {
        Permanent goblin = addCreatureReady(player1, new BloodstoneGoblin());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // First kicked creature
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(goblin.getPowerModifier()).isEqualTo(1);
        assertThat(goblin.getToughnessModifier()).isEqualTo(1);

        // Resolve the creature spell
        harness.passBothPriorities();

        // Second kicked creature
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(goblin.getPowerModifier()).isEqualTo(2);
        assertThat(goblin.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent casting a kicked spell does not trigger")
    void opponentKickedSpellDoesNotTrigger() {
        Permanent goblin = addCreatureReady(player1, new BloodstoneGoblin());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Opponent casts a kicked creature
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.WHITE, 6);
        harness.setHand(player2, List.of(new AcademyDrake()));
        harness.castKickedCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(goblin.getPowerModifier()).isEqualTo(0);
        assertThat(goblin.getToughnessModifier()).isEqualTo(0);
        assertThat(goblin.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
    }

    @Test
    @DisplayName("A kicked instant grants the bonus before the spell resolves")
    void kickedInstantTriggersBeforeResolution() {
        Permanent goblin = addCreatureReady(player1, new BloodstoneGoblin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AcademyDrake());
        harness.setHand(player1, List.of(new BlinkOfAnEye()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castKickedInstant(player1, 0, target.getId());

        assertThat(goblin.getPowerModifier()).isZero();
        assertThat(goblin.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
        harness.passBothPriorities();

        assertThat(goblin.getPowerModifier()).isEqualTo(1);
        assertThat(goblin.getToughnessModifier()).isEqualTo(1);
        assertThat(goblin.getGrantedKeywords()).contains(Keyword.MENACE);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The bonus and menace expire at the end of the turn")
    void bonusAndMenaceExpireAtEndOfTurn() {
        Permanent goblin = addCreatureReady(player1, new BloodstoneGoblin());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(goblin.getPowerModifier()).isEqualTo(1);
        assertThat(goblin.getToughnessModifier()).isEqualTo(1);
        assertThat(goblin.getGrantedKeywords()).contains(Keyword.MENACE);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(goblin.getPowerModifier()).isZero();
        assertThat(goblin.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Each Goblin receives its own bonus from a kicked spell")
    void multipleGoblinsReceiveTheirOwnBonus() {
        Permanent first = addCreatureReady(player1, new BloodstoneGoblin());
        Permanent second = addCreatureReady(player1, new BloodstoneGoblin());
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        for (Permanent goblin : List.of(first, second)) {
            assertThat(goblin.getPowerModifier()).isEqualTo(1);
            assertThat(goblin.getToughnessModifier()).isEqualTo(1);
            assertThat(goblin.getGrantedKeywords()).contains(Keyword.MENACE);
        }
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Returning the Goblin to hand before its trigger resolves does not boost another Goblin")
    void removedSourceDoesNotBoostAnotherGoblin() {
        Permanent removed = addCreatureReady(player1, new BloodstoneGoblin());
        Permanent remaining = addCreatureReady(player1, new BloodstoneGoblin());
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castKickedCreature(player1, 0);

        harness.setHand(player2, List.of(new BlinkOfAnEye()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, removed.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(removed);
        harness.assertInHand(player1, "Bloodstone Goblin");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(remaining.getPowerModifier()).isEqualTo(1);
        assertThat(remaining.getToughnessModifier()).isEqualTo(1);
        assertThat(remaining.getGrantedKeywords()).contains(Keyword.MENACE);
        assertThat(gd.stack).hasSize(1);
    }
}
