package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GorillaChieftainTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{G} grants a regeneration shield")
    void payGrantsRegenerationShield() {
        Permanent chieftain = addCreatureReady(player1, new GorillaChieftain());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(chieftain.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Gorilla Chieftain from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent chieftain = addCreatureReady(player1, new GorillaChieftain());
        chieftain.setRegenerationShield(1);
        chieftain.setBlocking(true);
        chieftain.addBlockingTarget(0);

        Permanent attacker = new Permanent(new com.github.laxika.magicalvibes.cards.h.HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gorilla Chieftain");
        assertThat(chieftain.isTapped()).isTrue();
        assertThat(chieftain.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gorilla Chieftain dies in combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent chieftain = addCreatureReady(player1, new GorillaChieftain());
        chieftain.setBlocking(true);
        chieftain.addBlockingTarget(0);

        Permanent attacker = new Permanent(new com.github.laxika.magicalvibes.cards.h.HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gorilla Chieftain");
        harness.assertInGraveyard(player1, "Gorilla Chieftain");
    }
}
