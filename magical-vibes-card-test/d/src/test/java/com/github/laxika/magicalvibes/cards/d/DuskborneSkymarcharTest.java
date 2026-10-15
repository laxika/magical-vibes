package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.s.SkymarchBloodletter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuskborneSkymarcher.class, SkymarchBloodletter.class, RaptorCompanion.class})
class DuskborneSkymarcharTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability on attacking Vampire puts it on the stack")
    void activatingOnAttackingVampirePutsOnStack() {
        addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent attacker = addAttackingVampire(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isInstanceOf(DuskborneSkymarcher.class);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Resolving ability gives attacking Vampire +1/+1")
    void resolvingBoostsAttackingVampire() {
        addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent attacker = addAttackingVampire(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating ability taps Duskborne Skymarcher")
    void activatingTapsSkymarcher() {
        Permanent skymarcher = addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent attacker = addAttackingVampire(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(skymarcher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without paying {W}")
    void cannotActivateWithoutMana() {
        addReadySkymarcher(player1);
        Permanent attacker = addAttackingVampire(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-attacking Vampire")
    void cannotTargetNonAttackingVampire() {
        addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent nonAttacker = addReadyVampire(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking Vampire");
    }

    @Test
    @DisplayName("Cannot target an attacking non-Vampire creature")
    void cannotTargetAttackingNonVampire() {
        addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent attacker = addAttackingNonVampire(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking Vampire");
    }

    @Test
    @DisplayName("Can target opponent's attacking Vampire")
    void canTargetOpponentAttackingVampire() {
        addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent opponentAttacker = addAttackingVampire(player2);

        harness.activateAbility(player1, 0, null, opponentAttacker.getId());
        harness.passBothPriorities();

        assertThat(opponentAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(opponentAttacker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent attacker = addAttackingVampire(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent attacker = addAttackingVampire(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        gd.playerBattlefields.get(player1.getId()).remove(attacker);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability does not boost a Vampire that stops attacking before resolution")
    void targetMustStillBeAttackingOnResolution() {
        addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent attacker = addAttackingVampire(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent skymarcher = addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent attacker = addAttackingVampire(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(skymarcher);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot pay the tap cost while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent skymarcher = addReadySkymarcher(player1);
        skymarcher.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent attacker = addAttackingVampire(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the tap cost while already tapped")
    void cannotActivateWhileTapped() {
        Permanent skymarcher = addReadySkymarcher(player1);
        skymarcher.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent attacker = addAttackingVampire(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySkymarcher(Player player) {
        return addCreatureReady(player, new DuskborneSkymarcher());
    }

    private Permanent addAttackingVampire(Player player) {
        Permanent perm = addCreatureReady(player, new SkymarchBloodletter());
        perm.setAttacking(true);
        return perm;
    }

    private Permanent addReadyVampire(Player player) {
        return addCreatureReady(player, new SkymarchBloodletter());
    }

    private Permanent addAttackingNonVampire(Player player) {
        Permanent perm = addCreatureReady(player, new RaptorCompanion());
        perm.setAttacking(true);
        return perm;
    }
}
