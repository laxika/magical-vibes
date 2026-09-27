package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DrossProwler;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NimShambler.class, Ornithopter.class, DrossProwler.class})
class NimShamblerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each artifact controlled by its controller")
    void getsPowerForControlledArtifacts() {
        harness.addToBattlefield(player1, new NimShambler());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        Permanent nim = findPermanent(player1, "Nim Shambler");

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nim)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a creature grants Nim Shambler a regeneration shield")
    void sacrificingCreatureRegenerates() {
        Permanent nim = addCreatureReady(player1, new NimShambler());
        Permanent fodder = addCreatureReady(player1, new DrossProwler());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(nim.getRegenerationShield()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Dross Prowler");
        harness.assertOnBattlefield(player1, "Nim Shambler");
    }

    @Test
    @DisplayName("Regeneration shield saves Nim Shambler from lethal combat damage")
    void regeneratesFromLethalCombatDamage() {
        Permanent nim = addCreatureReady(player1, new NimShambler());
        nim.setRegenerationShield(1);
        nim.setBlocking(true);
        nim.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new DrossProwler());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Nim Shambler");
        assertThat(findPermanent(player1, "Nim Shambler").getRegenerationShield()).isZero();
    }
}
