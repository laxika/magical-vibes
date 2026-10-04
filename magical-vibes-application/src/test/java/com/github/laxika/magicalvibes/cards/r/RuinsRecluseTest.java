package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(RuinsRecluse.class)
class RuinsRecluseTest extends BaseCardTest {

    @Test
    void activatingAbilityPutsPlusOnePlusOneCounterOnRuinsRecluse() {
        Permanent recluse = addReadyRecluse(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(recluse.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void activatingAbilityRequiresThreeGenericAndOneGreenMana() {
        addReadyRecluse(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void abilityCanBeActivatedMoreThanOnce() {
        Permanent recluse = addReadyRecluse(player1);
        addAbilityMana(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(recluse.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent addReadyRecluse(Player player) {
        Permanent recluse = new Permanent(new RuinsRecluse());
        recluse.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(recluse);
        return recluse;
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
