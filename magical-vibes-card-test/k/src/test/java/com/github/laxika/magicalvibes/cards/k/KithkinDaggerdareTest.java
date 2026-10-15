package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({KithkinDaggerdare.class, WoodlandChangeling.class})
class KithkinDaggerdareTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps Daggerdare and puts it on the stack")
    void activatingPutsOnStack() {
        Permanent daggerdare = addDaggerdareReady(player1);
        Permanent attacker = addAttackingCreature(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(daggerdare.isTapped()).isTrue();
        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Resolving ability gives attacking creature +2/+2")
    void resolvingBoostsAttackingCreature() {
        addDaggerdareReady(player1);
        Permanent attacker = addAttackingCreature(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isEqualTo(2);
        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        addDaggerdareReady(player1);
        Permanent nonAttacker = addCreatureReady(player1, new WoodlandChangeling());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking creature");
    }

    @Test
    @DisplayName("Can target opponent's attacking creature")
    void canTargetOpponentAttackingCreature() {
        addDaggerdareReady(player1);
        Permanent opponentAttacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, opponentAttacker.getId());
        harness.passBothPriorities();

        assertThat(opponentAttacker.getPowerModifier()).isEqualTo(2);
        assertThat(opponentAttacker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        addDaggerdareReady(player1);
        Permanent attacker = addAttackingCreature(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void cannotActivateWithoutGreenMana() {
        Permanent daggerdare = addDaggerdareReady(player1);
        Permanent attacker = addAttackingCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(daggerdare.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent daggerdare = addDaggerdareReady(player1);
        daggerdare.tap();
        Permanent attacker = addAttackingCreature(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent daggerdare = addDaggerdareReady(player1);
        daggerdare.setSummoningSick(true);
        Permanent attacker = addAttackingCreature(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(daggerdare.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotBoostTargetThatStopsAttackingBeforeResolution() {
        addDaggerdareReady(player1);
        Permanent attacker = addAttackingCreature(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent daggerdare = addDaggerdareReady(player1);
        Permanent attacker = addAttackingCreature(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, attacker.getId());

        gd.playerBattlefields.get(player1.getId()).remove(daggerdare);
        gd.playerGraveyards.get(player1.getId()).add(daggerdare.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isEqualTo(2);
    }

    private Permanent addDaggerdareReady(Player player) {
        return addCreatureReady(player, new KithkinDaggerdare());
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent perm = addCreatureReady(player, new WoodlandChangeling());
        perm.setAttacking(true);
        return perm;
    }
}
