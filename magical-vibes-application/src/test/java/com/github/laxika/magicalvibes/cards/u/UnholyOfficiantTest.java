package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(UnholyOfficiant.class)
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

    private Permanent addOfficiant(Player player, boolean summoningSick) {
        Permanent permanent = new Permanent(new UnholyOfficiant());
        permanent.setSummoningSick(summoningSick);
        gd.playerBattlefields.get(player.getId()).add(permanent);
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
