package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrespassingSouleaterTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Trespassing Souleater has one activated ability with Phyrexian blue cost")
    void hasUnblockableActivatedAbility() {
        TrespassingSouleater card = new TrespassingSouleater();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{U/P}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(MakeCreatureUnblockableEffect.class);
        MakeCreatureUnblockableEffect effect = (MakeCreatureUnblockableEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(effect.selfTargeting()).isTrue();
    }

    // ===== Activated ability: make self unblockable paying blue mana =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent souleater = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Trespassing Souleater");
        assertThat(entry.getTargetId()).isEqualTo(souleater.getId());
    }

    @Test
    @DisplayName("Resolving ability makes Trespassing Souleater unblockable this turn")
    void resolvingAbilityMakesUnblockable() {
        Permanent souleater = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(souleater.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable resets at end of turn cleanup")
    void unblockableResetsAtEndOfTurn() {
        Permanent souleater = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(souleater.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(souleater.isCantBeBlocked()).isFalse();
    }

    // ===== Phyrexian mana: pay with life =====

    @Test
    @DisplayName("Can pay Phyrexian mana with 2 life when no blue mana available")
    void paysLifeWhenNoBlueMana() {
        Permanent souleater = addSouleaterReady(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(souleater.isCantBeBlocked()).isTrue();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Prefers blue mana over life payment when available")
    void prefersBlueManaOverLife() {
        Permanent souleater = addSouleaterReady(player1);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(souleater.isCantBeBlocked()).isTrue();
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Activation constraints =====

    @Test
    @DisplayName("Activating ability does NOT tap Trespassing Souleater")
    void activatingAbilityDoesNotTap() {
        Permanent souleater = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(souleater.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent souleater = addSouleaterReady(player1);
        souleater.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Trespassing Souleater");
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness (no tap cost)")
    void canActivateWithSummoningSickness() {
        Permanent souleater = new Permanent(new TrespassingSouleater());
        gd.playerBattlefields.get(player1.getId()).add(souleater);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Trespassing Souleater");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if Trespassing Souleater is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addSouleaterReady(Player player) {
        Permanent perm = new Permanent(new TrespassingSouleater());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
