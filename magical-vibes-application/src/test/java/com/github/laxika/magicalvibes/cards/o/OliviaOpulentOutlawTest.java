package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Treasure;
import com.github.laxika.magicalvibes.cards.n.NighthawkScavenger;
import com.github.laxika.magicalvibes.cards.y.YoungPyromancer;
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

@CardUsed({OliviaOpulentOutlaw.class, GrizzlyBears.class, Treasure.class, NighthawkScavenger.class, YoungPyromancer.class})
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

    @Test
    void oliviaCreatesTreasureFromHerOwnCombatDamage() {
        addCreatureReady(player1, new OliviaOpulentOutlaw());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isOne();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void batchesOliviaAndAnotherRealOutlawIntoOneTrigger() {
        addCreatureReady(player1, new OliviaOpulentOutlaw());
        addCreatureReady(player1, new NighthawkScavenger());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    void nonOutlawCombatDamageDoesNotCreateTreasure() {
        addCreatureReady(player1, new OliviaOpulentOutlaw());
        addCreatureReady(player1, new YoungPyromancer());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void opposingOutlawDoesNotTriggerOlivia() {
        addCreatureReady(player1, new OliviaOpulentOutlaw()).tap();
        addCreatureReady(player2, new NighthawkScavenger());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    void cannotActivateDuringCombatEvenWithAllCostsAvailable() {
        Permanent olivia = addCreatureReady(player1, new OliviaOpulentOutlaw());
        addTreasure(player1);
        addTreasure(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(olivia), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        assertThat(olivia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countersUseControlledCreaturesAtResolutionAndCostsArePaidImmediately() {
        Permanent olivia = addCreatureReady(player1, new OliviaOpulentOutlaw());
        Permanent opponent = addCreatureReady(player2, new YoungPyromancer());
        addTreasure(player1);
        addTreasure(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, indexOf(olivia), 0, null, null);

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(olivia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent newcomer = addCreatureReady(player1, new YoungPyromancer());
        harness.passBothPriorities();

        assertThat(olivia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
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
