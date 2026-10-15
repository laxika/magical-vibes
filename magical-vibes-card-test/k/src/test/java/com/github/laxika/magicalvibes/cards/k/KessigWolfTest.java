package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Keyword;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KessigWolf.class})
class KessigWolfTest extends BaseCardTest {

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
        harness.assertOnBattlefield(player1, "Kessig Wolf");
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
        harness.addToBattlefield(player1, new KessigWolf());
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

    @Test
    @DisplayName("Ability resolves without affecting a replacement Wolf after its source leaves")
    void abilityDoesNotAffectReplacementWolf() {
        addWolfReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new KessigWolf());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Generic mana may be paid with another color, but red is required")
    void acceptsMixedManaPayment() {
        Permanent wolf = addWolfReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Two nonred mana cannot pay the activation cost")
    void rejectsPaymentWithoutRedMana() {
        addWolfReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("First strike is granted only to the source Wolf")
    void grantsFirstStrikeOnlyToSource() {
        Permanent source = addWolfReady(player1);
        Permanent ally = addWolfReady(player1);
        Permanent opponent = addWolfReady(player2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addWolfReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new KessigWolf());
        perm.setSummoningSick(false);
        return perm;
    }
}
