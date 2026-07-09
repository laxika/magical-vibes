package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SkirkProspector;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MadAuntieTest extends BaseCardTest {

    // ===== Static anthem: other Goblins you control get +1/+1 =====

    @Test
    @DisplayName("Other Goblin creatures you control get +1/+1")
    void buffsOtherGoblinsYouControl() {
        Permanent goblin = addCreatureReady(player1, new SkirkProspector());
        int basePower = gqs.getEffectivePower(gd, goblin);
        int baseToughness = gqs.getEffectiveToughness(gd, goblin);

        harness.addToBattlefield(player1, new MadAuntie());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Mad Auntie does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new MadAuntie());

        Permanent auntie = findPermanent(player1, "Mad Auntie");

        assertThat(gqs.getEffectivePower(gd, auntie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, auntie)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Goblin creatures")
    void doesNotBuffNonGoblins() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new MadAuntie());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Goblin creatures")
    void doesNotBuffOpponentGoblins() {
        Permanent opponentGoblin = addCreatureReady(player2, new SkirkProspector());
        int basePower = gqs.getEffectivePower(gd, opponentGoblin);

        harness.addToBattlefield(player1, new MadAuntie());

        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(basePower);
    }

    // ===== {T}: Regenerate another target Goblin =====

    @Test
    @DisplayName("Activating regeneration targets a Goblin and puts ability on stack")
    void activatingRegenTargetsGoblin() {
        addCreatureReady(player1, new MadAuntie());
        Permanent goblin = addCreatureReady(player1, new SkirkProspector());

        harness.activateAbility(player1, 0, null, goblin.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(goblin.getId());
    }

    @Test
    @DisplayName("Resolving regeneration grants a shield to the target Goblin")
    void resolvingRegenGrantsShield() {
        addCreatureReady(player1, new MadAuntie());
        Permanent goblin = addCreatureReady(player1, new SkirkProspector());

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(goblin.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can regenerate an opponent's Goblin")
    void canRegenerateOpponentGoblin() {
        addCreatureReady(player1, new MadAuntie());
        Permanent opponentGoblin = addCreatureReady(player2, new SkirkProspector());

        harness.activateAbility(player1, 0, null, opponentGoblin.getId());
        harness.passBothPriorities();

        assertThat(opponentGoblin.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target itself (another target Goblin)")
    void cannotTargetItself() {
        Permanent auntie = addCreatureReady(player1, new MadAuntie());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, auntie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-Goblin creature")
    void cannotTargetNonGoblin() {
        addCreatureReady(player1, new MadAuntie());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
