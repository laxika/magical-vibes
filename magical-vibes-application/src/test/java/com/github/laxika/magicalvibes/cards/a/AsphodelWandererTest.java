package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AsphodelWandererTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration grants a regeneration shield")
    void activatingRegenerationGrantsShield() {
        Permanent wanderer = addWandererReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(wanderer.getId());

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Asphodel Wanderer").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A regeneration shield saves Asphodel Wanderer from lethal damage")
    void regenerationShieldSavesFromLethalDamage() {
        addWandererReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wanderer = findPermanent(player1, "Asphodel Wanderer");
        wanderer.setBlocking(true);
        wanderer.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Asphodel Wanderer")).isNotNull();
        assertThat(findPermanent(player1, "Asphodel Wanderer").getRegenerationShield()).isZero();
    }

    private Permanent addWandererReady(Player player) {
        Permanent perm = new Permanent(new AsphodelWanderer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
