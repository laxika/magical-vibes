package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BishopsSoldier;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IxallisKeeper.class, BishopsSoldier.class})
class IxallisKeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addCreatureReady(player1, new IxallisKeeper());
        addCreatureReady(player1, new BishopsSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Bishop's Soldier"));

        harness.assertNotOnBattlefield(player1, "Ixalli's Keeper");
        harness.assertInGraveyard(player1, "Ixalli's Keeper");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating ability requires {7}{G} mana")
    void requiresMana() {
        addCreatureReady(player1, new IxallisKeeper());
        addCreatureReady(player1, new BishopsSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Bishop's Soldier")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate when summoning sick because ability requires tap")
    void cannotActivateWhenSummoningSick() {
        harness.addToBattlefield(player1, new IxallisKeeper());
        addCreatureReady(player1, new BishopsSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Bishop's Soldier")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability gives target creature +5/+5 and trample")
    void resolvingBoostsWithTrample() {
        addCreatureReady(player1, new IxallisKeeper());
        Permanent soldier = addCreatureReady(player1, new BishopsSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Bishop's Soldier"));
        harness.passBothPriorities();

        // Ixalli's Keeper is sacrificed
        harness.assertNotOnBattlefield(player1, "Ixalli's Keeper");
        harness.assertInGraveyard(player1, "Ixalli's Keeper");

        // Bishop's Soldier (2/2) gets +5/+5 = 7/7
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(7);

        // Bishop's Soldier gains trample
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentCreature() {
        addCreatureReady(player1, new IxallisKeeper());
        Permanent opponentSoldier = addCreatureReady(player2, new BishopsSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Bishop's Soldier"));
        harness.passBothPriorities();

        // Opponent's creature gets +5/+5 and trample
        assertThat(gqs.getEffectivePower(gd, opponentSoldier)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, opponentSoldier)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, opponentSoldier, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Boost and trample wear off at cleanup step")
    void boostAndTrampleWearOffAtCleanup() {
        addCreatureReady(player1, new IxallisKeeper());
        Permanent soldier = addCreatureReady(player1, new BishopsSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Bishop's Soldier"));
        harness.passBothPriorities();

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Can target itself, but the sacrificed target makes the ability fail to resolve")
    void canTargetItself() {
        Permanent keeper = addCreatureReady(player1, new IxallisKeeper());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, keeper.getId());

        harness.assertNotOnBattlefield(player1, "Ixalli's Keeper");
        harness.assertInGraveyard(player1, "Ixalli's Keeper");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate a tapped Keeper")
    void cannotActivateWhenTapped() {
        Permanent keeper = addCreatureReady(player1, new IxallisKeeper());
        keeper.tap();
        Permanent target = addCreatureReady(player1, new BishopsSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Ixalli's Keeper");
        harness.assertNotInGraveyard(player1, "Ixalli's Keeper");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Eight colorless mana cannot pay the required green mana")
    void requiresGreenMana() {
        addCreatureReady(player1, new IxallisKeeper());
        Permanent target = addCreatureReady(player1, new BishopsSoldier());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Ixalli's Keeper");
        harness.assertNotInGraveyard(player1, "Ixalli's Keeper");
        assertThat(gd.stack).isEmpty();
    }
}
