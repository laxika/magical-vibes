package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TreasonousOgre.class)
class TreasonousOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Pays 3 life to add {R}")
    void paysLifeForRedMana() {
        addCreatureReady(player1, new TreasonousOgre());
        GameData gd = harness.getGameData();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the activation cost without 3 life")
    void cannotPayActivationCostWithoutEnoughLife() {
        addCreatureReady(player1, new TreasonousOgre());
        harness.setLife(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Dethrone puts a +1/+1 counter on the attacking Ogre")
    void dethroneTriggersWhenAttackingPlayerWithMostLife() {
        Permanent ogre = addCreatureReady(player1, new TreasonousOgre());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ogre)));
        harness.passBothPriorities();

        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
