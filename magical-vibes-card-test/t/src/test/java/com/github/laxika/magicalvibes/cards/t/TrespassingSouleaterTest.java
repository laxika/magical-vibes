package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TrespassingSouleater.class})
class TrespassingSouleaterTest extends BaseCardTest {


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
        harness.addToBattlefield(player1, new TrespassingSouleater());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Trespassing Souleater");
    }


    @Test
    @DisplayName("Ability resolves without affecting a replacement Souleater if its source leaves")
    void abilityDoesNotAffectReplacementSource() {
        Permanent original = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();
        Permanent replacement = addSouleaterReady(player1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(original.isCantBeBlocked()).isFalse();
        assertThat(replacement.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot pay two life with only one life and no blue mana")
    void cannotActivateWithoutEnoughLifeOrMana() {
        addSouleaterReady(player1);
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 1);
    }

    @Test
    @DisplayName("Each activation pays its cost even if already unblockable")
    void repeatedActivationPaysLifeAgain() {
        Permanent souleater = addSouleaterReady(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        assertThat(souleater.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Resolving the ability prevents an opposing creature from blocking")
    void preventsBlockingOnlyTheSource() {
        Permanent souleater = addSouleaterReady(player1);
        Permanent other = addSouleaterReady(player1);
        Permanent blocker = addSouleaterReady(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(harness.getBlockLegalityService().canBlockAttacker(
                gd, blocker, souleater, gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(harness.getBlockLegalityService().canBlockAttacker(
                gd, blocker, other, gd.playerBattlefields.get(player2.getId()))).isTrue();
    }


    private Permanent addSouleaterReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TrespassingSouleater());
        perm.setSummoningSick(false);
        return perm;
    }
}
