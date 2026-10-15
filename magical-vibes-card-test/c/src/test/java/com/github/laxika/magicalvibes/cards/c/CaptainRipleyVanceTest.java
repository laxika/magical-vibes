package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainRipleyVance.class, LightningBolt.class, Bonesplitter.class})
class CaptainRipleyVanceTest extends BaseCardTest {

    @Test
    @DisplayName("The third spell puts a counter on Captain Ripley Vance and deals damage equal to its power")
    void thirdSpellPutsCounterAndDealsPowerDamage() {
        Permanent ripley = addCreatureReady(player1, new CaptainRipleyVance());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        castBoltAndResolve();
        castBoltAndResolve();
        harness.castInstant(player1, 0, player2.getId());

        assertThat(ripley.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ripley.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(7);
    }

    @Test
    @DisplayName("The first two spells do not trigger Captain Ripley Vance")
    void firstTwoSpellsDoNotTrigger() {
        Permanent ripley = addCreatureReady(player1, new CaptainRipleyVance());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        castBoltAndResolve();
        castBoltAndResolve();

        assertThat(ripley.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    void fourthSpellDoesNotTriggerAgain() {
        Permanent ripley = addCreatureReady(player1, new CaptainRipleyVance());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 4);

        castBoltAndResolve();
        castBoltAndResolve();
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        castBoltAndResolve();

        assertThat(ripley.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(4);
    }

    @Test
    void countsSpellsCastBeforeEnteringBattlefield() {
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        castBoltAndResolve();
        castBoltAndResolve();
        Permanent ripley = addCreatureReady(player1, new CaptainRipleyVance());

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ripley.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(7);
    }

    @Test
    void castingRipleyAsThirdSpellDoesNotTriggerItsOwnAbility() {
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new CaptainRipleyVance()));
        harness.addMana(player1, ManaColor.RED, 5);
        castBoltAndResolve();
        castBoltAndResolve();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Captain Ripley Vance").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsSpellsDoNotTriggerAbilityOrCountTowardThirdSpell() {
        Permanent ripley = addCreatureReady(player1, new CaptainRipleyVance());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        castBoltAndResolve();

        assertThat(ripley.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void abilityTriggersDuringOpponentsTurn() {
        Permanent ripley = addCreatureReady(player1, new CaptainRipleyVance());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        castBoltAndResolve();
        castBoltAndResolve();

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ripley.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(7);
    }

    @Test
    void removedSourceStillDealsDamageUsingLastKnownPowerWithoutAddingCounter() {
        Permanent ripley = addCreatureReady(player1, new CaptainRipleyVance());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        castBoltAndResolve();
        castBoltAndResolve();
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, ripley.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ripley);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ripley.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(8);
    }

    @Test
    void illegalDamageTargetPreventsCounterAsWell() {
        Permanent ripley = addCreatureReady(player1, new CaptainRipleyVance());
        Permanent target = addCreatureReady(player2, new CaptainRipleyVance());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 4);
        castBoltAndResolve();
        castBoltAndResolve();
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ripley.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    void removedSourceRetainsEquipmentBonusInLastKnownPower() {
        Permanent ripley = addCreatureReady(player1, new CaptainRipleyVance());
        harness.addToBattlefield(player1, new Bonesplitter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, ripley.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        castBoltAndResolve();
        castBoltAndResolve();
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, ripley.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ripley);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ripley.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(6);
    }

    private void castBoltAndResolve() {
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }
}
