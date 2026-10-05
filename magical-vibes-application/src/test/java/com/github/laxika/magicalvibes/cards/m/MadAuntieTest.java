package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MadAuntie.class, MudbuttonTorchrunner.class, GoldmeadowStalwart.class,
        BoggartShenanigans.class, Tarfire.class})
class MadAuntieTest extends BaseCardTest {

    @Test
    @DisplayName("Other Goblin creatures you control get +1/+1")
    void buffsOtherGoblinsYouControl() {
        Permanent goblin = addCreatureReady(player1, new MudbuttonTorchrunner());
        int basePower = gqs.getEffectivePower(gd, goblin);
        int baseToughness = gqs.getEffectiveToughness(gd, goblin);

        harness.addToBattlefield(player1, new MadAuntie());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Mad Auntie does not buff itself")
    void doesNotBuffItself() {
        Permanent auntie = harness.addToBattlefieldAndReturn(player1, new MadAuntie());

        assertThat(gqs.getEffectivePower(gd, auntie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, auntie)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Goblin creatures")
    void doesNotBuffNonGoblins() {
        Permanent stalwart = addCreatureReady(player1, new GoldmeadowStalwart());
        harness.addToBattlefield(player1, new MadAuntie());

        assertThat(gqs.getEffectivePower(gd, stalwart)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stalwart)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Goblin creatures")
    void doesNotBuffOpponentGoblins() {
        Permanent opponentGoblin = addCreatureReady(player2, new MudbuttonTorchrunner());
        int basePower = gqs.getEffectivePower(gd, opponentGoblin);

        harness.addToBattlefield(player1, new MadAuntie());

        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Activating regeneration targets a Goblin and puts ability on stack")
    void activatingRegenTargetsGoblin() {
        addCreatureReady(player1, new MadAuntie());
        Permanent goblin = addCreatureReady(player1, new MudbuttonTorchrunner());

        harness.activateAbility(player1, 0, null, goblin.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(goblin.getId());
    }

    @Test
    @DisplayName("Resolving regeneration grants a shield to the target Goblin")
    void resolvingRegenGrantsShield() {
        addCreatureReady(player1, new MadAuntie());
        Permanent goblin = addCreatureReady(player1, new MudbuttonTorchrunner());

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(goblin.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can regenerate an opponent's Goblin")
    void canRegenerateOpponentGoblin() {
        addCreatureReady(player1, new MadAuntie());
        Permanent opponentGoblin = addCreatureReady(player2, new MudbuttonTorchrunner());

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
        Permanent stalwart = addCreatureReady(player1, new GoldmeadowStalwart());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, stalwart.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two Mad Aunties boost each other")
    void twoAuntiesBoostEachOther() {
        Permanent first = addCreatureReady(player1, new MadAuntie());
        Permanent second = addCreatureReady(player1, new MadAuntie());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Regeneration can target a noncreature Goblin permanent")
    void canRegenerateNoncreatureGoblin() {
        addCreatureReady(player1, new MadAuntie());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());

        harness.activateAbility(player1, 0, null, enchantment.getId());
        harness.passBothPriorities();

        assertThat(enchantment.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Summoning sickness prevents the tap ability")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new MadAuntie());
        Permanent goblin = addCreatureReady(player1, new MudbuttonTorchrunner());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regeneration prevents lethal damage and consumes the shield")
    void regenerationPreventsLethalDamage() {
        Permanent auntie = addCreatureReady(player1, new MadAuntie());
        Permanent goblin = addCreatureReady(player1, new MudbuttonTorchrunner());
        harness.activateAbility(player1, 0, null, goblin.getId());
        assertThat(auntie.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Tarfire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, goblin.getId());

        harness.assertOnBattlefield(player1, "Mudbutton Torchrunner");
        harness.assertNotInGraveyard(player1, "Mudbutton Torchrunner");
        assertThat(goblin.isTapped()).isTrue();
        assertThat(goblin.getRegenerationShield()).isZero();
    }
}
