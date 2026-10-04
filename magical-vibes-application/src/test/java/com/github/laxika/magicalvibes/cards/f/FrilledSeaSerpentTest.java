package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrilledSeaSerpent.class})
class FrilledSeaSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability makes Frilled Sea Serpent unblockable this turn")
    void abilityMakesSelfUnblockable() {
        Permanent serpent = addSerpent(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(serpent.isCantBeBlocked()).isTrue();
        assertThat(serpent.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Unblockable wears off during cleanup")
    void unblockableWearsOff() {
        Permanent serpent = addSerpent(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(serpent.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(serpent.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Ability cannot be activated without enough mana")
    void abilityRequiresMana() {
        Permanent serpent = addSerpent(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(serpent.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Ability only affects its source and only after resolution")
    void affectsOnlySourceAfterResolution() {
        Permanent serpent = addSerpent(player1);
        Permanent otherSerpent = addSerpent(player1);
        Permanent opposingSerpent = addSerpent(player2);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(serpent.isCantBeBlocked()).isFalse();

        harness.passBothPriorities();

        assertThat(serpent.isCantBeBlocked()).isTrue();
        assertThat(otherSerpent.isCantBeBlocked()).isFalse();
        assertThat(opposingSerpent.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent serpent = addSerpent(player1);
        serpent.setTapped(true);
        serpent.setSummoningSick(true);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(serpent.isCantBeBlocked()).isTrue();
        assertThat(serpent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Seven total mana is insufficient without two blue mana")
    void requiresTwoBlueMana() {
        Permanent serpent = addSerpent(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(serpent.isCantBeBlocked()).isFalse();
    }

    private Permanent addSerpent(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new FrilledSeaSerpent());
        perm.setSummoningSick(false);
        return perm;
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 5);
    }
}
