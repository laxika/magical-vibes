package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InsatiableSouleaterTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Insatiable Souleater has one activated ability granting trample with Phyrexian green cost")
    void hasTrampleActivatedAbility() {
        InsatiableSouleater card = new InsatiableSouleater();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{G/P}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect trample = (GrantKeywordEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(trample.keyword()).isEqualTo(Keyword.TRAMPLE);
        assertThat(trample.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Activated ability: grant trample paying green mana =====

    @Test
    @DisplayName("Activating trample ability puts it on the stack")
    void activatingTramplePutsOnStack() {
        Permanent souleater = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Insatiable Souleater");
        assertThat(entry.getTargetId()).isEqualTo(souleater.getId());
    }

    @Test
    @DisplayName("Resolving trample ability grants trample until end of turn")
    void resolvingTrampleAbilityGrantsTrample() {
        Permanent souleater = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, souleater, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample granted by ability resets at end of turn cleanup")
    void trampleResetsAtEndOfTurn() {
        Permanent souleater = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, souleater, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, souleater, Keyword.TRAMPLE)).isFalse();
    }

    // ===== Phyrexian mana: pay with life =====

    @Test
    @DisplayName("Can pay Phyrexian mana with 2 life when no green mana available")
    void paysLifeWhenNoGreenMana() {
        Permanent souleater = addSouleaterReady(player1);
        harness.setLife(player1, 20);
        // No mana added — Phyrexian mana auto-pays with 2 life

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, souleater, Keyword.TRAMPLE)).isTrue();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Prefers green mana over life payment when available")
    void prefersGreenManaOverLife() {
        Permanent souleater = addSouleaterReady(player1);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, souleater, Keyword.TRAMPLE)).isTrue();
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Activation constraints =====

    @Test
    @DisplayName("Activating ability does NOT tap Insatiable Souleater")
    void activatingAbilityDoesNotTap() {
        Permanent souleater = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(souleater.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent souleater = addSouleaterReady(player1);
        souleater.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Insatiable Souleater");
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness (no tap cost)")
    void canActivateWithSummoningSickness() {
        Permanent souleater = new Permanent(new InsatiableSouleater());
        gd.playerBattlefields.get(player1.getId()).add(souleater);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Insatiable Souleater");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if Insatiable Souleater is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addSouleaterReady(Player player) {
        Permanent perm = new Permanent(new InsatiableSouleater());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
