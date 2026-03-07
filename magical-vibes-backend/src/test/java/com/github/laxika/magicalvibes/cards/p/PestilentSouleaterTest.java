package com.github.laxika.magicalvibes.cards.p;

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

import static org.assertj.core.api.Assertions.assertThat;

class PestilentSouleaterTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Pestilent Souleater has one activated ability granting infect with Phyrexian black cost")
    void hasInfectActivatedAbility() {
        PestilentSouleater card = new PestilentSouleater();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{B/P}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect infect = (GrantKeywordEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(infect.keyword()).isEqualTo(Keyword.INFECT);
        assertThat(infect.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Activated ability: grant infect paying black mana =====

    @Test
    @DisplayName("Activating infect ability puts it on the stack")
    void activatingInfectPutsOnStack() {
        Permanent souleater = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Pestilent Souleater");
        assertThat(entry.getTargetPermanentId()).isEqualTo(souleater.getId());
    }

    @Test
    @DisplayName("Resolving infect ability grants infect until end of turn")
    void resolvingInfectAbilityGrantsInfect() {
        Permanent souleater = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, souleater, Keyword.INFECT)).isTrue();
    }

    @Test
    @DisplayName("Infect granted by ability resets at end of turn cleanup")
    void infectResetsAtEndOfTurn() {
        Permanent souleater = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, souleater, Keyword.INFECT)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, souleater, Keyword.INFECT)).isFalse();
    }

    // ===== Phyrexian mana: pay with life =====

    @Test
    @DisplayName("Can pay Phyrexian mana with 2 life when no black mana available")
    void paysLifeWhenNoBlackMana() {
        Permanent souleater = addSouleaterReady(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, souleater, Keyword.INFECT)).isTrue();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Prefers black mana over life payment when available")
    void prefersBlackManaOverLife() {
        Permanent souleater = addSouleaterReady(player1);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, souleater, Keyword.INFECT)).isTrue();
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Activation constraints =====

    @Test
    @DisplayName("Activating ability does NOT tap Pestilent Souleater")
    void activatingAbilityDoesNotTap() {
        Permanent souleater = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(souleater.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent souleater = addSouleaterReady(player1);
        souleater.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pestilent Souleater");
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness (no tap cost)")
    void canActivateWithSummoningSickness() {
        Permanent souleater = new Permanent(new PestilentSouleater());
        gd.playerBattlefields.get(player1.getId()).add(souleater);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pestilent Souleater");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if Pestilent Souleater is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addSouleaterReady(Player player) {
        Permanent perm = new Permanent(new PestilentSouleater());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
