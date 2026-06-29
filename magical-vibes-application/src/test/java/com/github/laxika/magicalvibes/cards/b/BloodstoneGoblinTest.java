package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodstoneGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a kicked creature triggers +1/+1 and menace")
    void kickedCreatureTriggersBoostAndMenace() {
        Permanent goblin = addReadyGoblin(player1);

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
        Permanent goblin = addReadyGoblin(player1);

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
        Permanent goblin = addReadyGoblin(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(goblin.getPowerModifier()).isEqualTo(0);
        assertThat(goblin.getToughnessModifier()).isEqualTo(0);
        assertThat(goblin.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
    }

    @Test
    @DisplayName("Multiple kicked spells stack the boost")
    void multipleKickedSpellsStackBoost() {
        Permanent goblin = addReadyGoblin(player1);

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
        Permanent goblin = addReadyGoblin(player1);

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

    // ===== Helpers =====

    private Permanent addReadyGoblin(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new BloodstoneGoblin());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
