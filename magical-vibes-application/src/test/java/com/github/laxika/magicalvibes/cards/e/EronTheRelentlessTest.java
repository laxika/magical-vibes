package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(EronTheRelentless.class)
class EronTheRelentlessTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the activated ability grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        addCreatureReady(player1, new EronTheRelentless());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent eron = findPermanent(player1, "Eron the Relentless");
        assertThat(eron.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Eron from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent perm = addCreatureReady(player1, new EronTheRelentless());
        perm.setRegenerationShield(1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new EronTheRelentless());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Eron the Relentless");
        Permanent eron = findPermanent(player1, "Eron the Relentless");
        assertThat(eron.isTapped()).isTrue();
        assertThat(eron.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Eron dies without a regeneration shield")
    void diesWithoutRegenShield() {
        Permanent perm = addCreatureReady(player1, new EronTheRelentless());
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new EronTheRelentless());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Eron the Relentless");
        harness.assertInGraveyard(player1, "Eron the Relentless");
    }

    @Test
    @DisplayName("Eron can activate its regeneration ability while tapped")
    void canActivateWhenTapped() {
        Permanent eron = addCreatureReady(player1, new EronTheRelentless());
        eron.tap();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(eron.getRegenerationShield()).isEqualTo(1);
        assertThat(eron.isTapped()).isTrue();
    }
}
