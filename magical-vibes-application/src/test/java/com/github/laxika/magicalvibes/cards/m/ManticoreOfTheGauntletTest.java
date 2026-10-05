package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.n.NissaStewardOfElements;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManticoreOfTheGauntlet.class, Colossapede.class, NissaStewardOfElements.class})
class ManticoreOfTheGauntletTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a -1/-1 counter on a creature you control and deals 3 damage to an opponent")
    void etbCountersOwnCreatureAndDamagesOpponent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        harness.setHand(player1, List.of(new ManticoreOfTheGauntlet()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        // Target order matches declaration: counter-on-your-creature first, damage second.
        harness.castCreature(player1, 0, List.of(creature.getId(), player2.getId()));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
        // Opponent takes 3 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("ETB cannot target its controller with opponent damage")
    void etbCannotDamageController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        harness.setHand(player1, List.of(new ManticoreOfTheGauntlet()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(creature.getId(), player1.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent or planeswalker");
    }

    @Test
    @DisplayName("ETB cannot put the counter on a creature you don't control")
    void etbCannotCounterOpponentCreature() {
        harness.addToBattlefield(player2, new Colossapede());
        UUID opponentCreature = harness.getPermanentId(player2, "Colossapede");
        harness.setHand(player1, List.of(new ManticoreOfTheGauntlet()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(opponentCreature, player2.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("ETB can deal 3 damage to a planeswalker")
    void etbDamagesPlaneswalker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        Permanent nissa = harness.addToBattlefieldAndReturn(player2, new NissaStewardOfElements());
        nissa.setCounterCount(CounterType.LOYALTY, 4);

        harness.setHand(player1, List.of(new ManticoreOfTheGauntlet()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, List.of(creature.getId(), nissa.getId()));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(1); // 4 - 3
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void enteringManticoreCanTargetItself() {
        Permanent manticore = harness.enterBattlefieldAndReturn(player1, new ManticoreOfTheGauntlet());
        harness.handlePermanentChosen(player1, manticore.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(manticore.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damageCanTargetOwnPlaneswalker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        Permanent nissa = harness.addToBattlefieldAndReturn(player1, new NissaStewardOfElements());
        nissa.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new ManticoreOfTheGauntlet()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0, List.of(creature.getId(), nissa.getId()));
        resolveAllTriggers();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void damageStillResolvesWhenCounterTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        harness.setHand(player1, List.of(new ManticoreOfTheGauntlet()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0, List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counterStillResolvesWhenPlaneswalkerTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        Permanent nissa = harness.addToBattlefieldAndReturn(player2, new NissaStewardOfElements());
        nissa.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new ManticoreOfTheGauntlet()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0, List.of(creature.getId(), nissa.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(nissa);
        gd.playerGraveyards.get(player2.getId()).add(nissa.getCard());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }
}
