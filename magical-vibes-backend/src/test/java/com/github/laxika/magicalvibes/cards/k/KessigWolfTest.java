package com.github.laxika.magicalvibes.cards.k;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KessigWolfTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Kessig Wolf has one activated ability granting first strike")
    void hasOneActivatedAbility() {
        KessigWolf card = new KessigWolf();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isEqualTo("{1}{R}");
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(GrantKeywordEffect.class);

        GrantKeywordEffect effect = (GrantKeywordEffect) ability.getEffects().getFirst();
        assertThat(effect.keyword()).isEqualTo(Keyword.FIRST_STRIKE);
        assertThat(effect.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Kessig Wolf puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new KessigWolf()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Kessig Wolf");
    }

    @Test
    @DisplayName("Resolving puts Kessig Wolf onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new KessigWolf()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Kessig Wolf"));
    }

    // ===== First strike ability =====

    @Test
    @DisplayName("Activating ability puts GrantKeywordToSelf on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent wolf = addWolfReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Kessig Wolf");
        assertThat(entry.getTargetId()).isEqualTo(wolf.getId());
    }

    @Test
    @DisplayName("Resolving ability grants first strike until end of turn")
    void resolvingAbilityGrantsFirstStrike() {
        Permanent wolf = addWolfReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike resets at end of turn cleanup")
    void firstStrikeResetsAtEndOfTurn() {
        Permanent wolf = addWolfReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Activation constraints =====

    @Test
    @DisplayName("Activating ability does not tap Kessig Wolf")
    void activatingAbilityDoesNotTap() {
        Permanent wolf = addWolfReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(wolf.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent wolf = addWolfReady(player1);
        wolf.tap();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        Permanent wolf = new Permanent(new KessigWolf());
        gd.playerBattlefields.get(player1.getId()).add(wolf);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Requires {1}{R} to activate — not enough with only 1 red")
    void requiresOneGenericAndOneRed() {
        addWolfReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate same ability multiple times")
    void canActivateMultipleTimes() {
        Permanent wolf = addWolfReady(player1);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.FIRST_STRIKE)).isTrue();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if Kessig Wolf is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addWolfReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addWolfReady(Player player) {
        Permanent perm = new Permanent(new KessigWolf());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
