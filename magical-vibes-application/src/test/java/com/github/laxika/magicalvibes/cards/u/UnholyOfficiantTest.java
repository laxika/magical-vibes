package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnholyOfficiant.class})
class UnholyOfficiantTest extends BaseCardTest {

    @Test
    void activatedAbilityPutsCounterOnSelf() {
        Permanent officiant = addOfficiant(player1, true);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(officiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void activatedAbilityCanBeUsedMultipleTimes() {
        Permanent officiant = addOfficiant(player1, false);
        addAbilityMana(player1, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(officiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void activationRequiresFiveManaIncludingWhite() {
        addOfficiant(player1, false);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void counterIsAddedOnlyWhenAbilityResolves() {
        Permanent officiant = addOfficiant(player1, false);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(officiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(officiant.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(officiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedCreatureCanActivateAbility() {
        Permanent officiant = addOfficiant(player1, true);
        officiant.tap();
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(officiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(officiant.isTapped()).isTrue();
    }

    @Test
    void fiveColorlessManaCannotPayWhiteRequirement() {
        addOfficiant(player1, false);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void whiteManaStillRequiresFourAdditionalMana() {
        addOfficiant(player1, false);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void attackingDoesNotTapOfficiant() {
        Permanent officiant = addOfficiant(player1, false);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(officiant.isAttacking()).isTrue();
        assertThat(officiant.isTapped()).isFalse();
    }

    private Permanent addOfficiant(Player player, boolean summoningSick) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new UnholyOfficiant());
        permanent.setSummoningSick(summoningSick);
        return permanent;
    }

    private void addAbilityMana(Player player) {
        addAbilityMana(player, 1);
    }

    private void addAbilityMana(Player player, int activations) {
        harness.addMana(player, ManaColor.WHITE, activations);
        harness.addMana(player, ManaColor.COLORLESS, activations * 4);
    }
}
