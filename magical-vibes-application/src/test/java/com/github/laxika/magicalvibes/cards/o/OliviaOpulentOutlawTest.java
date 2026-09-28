package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Treasure;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OliviaOpulentOutlaw.class, GrizzlyBears.class, Treasure.class})
class OliviaOpulentOutlawTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Treasure when one or more outlaws deal combat damage")
    void createsOneTreasureForMultipleOutlaws() {
        addCreatureReady(player1, new OliviaOpulentOutlaw());
        addOutlaw(player1);
        addOutlaw(player1);

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    @DisplayName("Sacrificing two Treasures puts two counters on each creature you control")
    void sacrificesTwoTreasuresToPutCountersOnCreatures() {
        Permanent olivia = addCreatureReady(player1, new OliviaOpulentOutlaw());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addTreasure(player1);
        addTreasure(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(olivia), 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(olivia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without two Treasures")
    void cannotActivateWithoutTwoTreasures() {
        Permanent olivia = addCreatureReady(player1, new OliviaOpulentOutlaw());
        addTreasure(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(olivia), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    private Permanent addOutlaw(Player player) {
        Permanent outlaw = addCreatureReady(player, new GrizzlyBears());
        TestCards.mutableCard(outlaw).setSubtypes(List.of(CardSubtype.ASSASSIN));
        return outlaw;
    }

    private void addTreasure(Player player) {
        harness.addToBattlefield(player, new Treasure());
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
