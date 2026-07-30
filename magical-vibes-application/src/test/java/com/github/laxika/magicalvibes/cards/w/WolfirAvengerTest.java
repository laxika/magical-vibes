package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WolfirAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{G} grants a regeneration shield")
    void payGrantsRegenerationShield() {
        Permanent avenger = addCreatureReady(player1, new WolfirAvenger());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(avenger.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Wolfir Avenger from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent avenger = addCreatureReady(player1, new WolfirAvenger());
        avenger.setRegenerationShield(1);
        avenger.setBlocking(true);
        avenger.addBlockingTarget(0);

        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wolfir Avenger");
        assertThat(avenger.isTapped()).isTrue();
        assertThat(avenger.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Wolfir Avenger dies in combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent avenger = addCreatureReady(player1, new WolfirAvenger());
        avenger.setBlocking(true);
        avenger.addBlockingTarget(0);

        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wolfir Avenger");
        harness.assertInGraveyard(player1, "Wolfir Avenger");
    }
}
