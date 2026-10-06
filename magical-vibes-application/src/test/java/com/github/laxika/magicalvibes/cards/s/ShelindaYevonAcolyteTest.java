package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShelindaYevonAcolyte.class, GrizzlyBears.class, SuntailHawk.class,
        GloriousAnthem.class, Unsummon.class})
class ShelindaYevonAcolyteTest extends BaseCardTest {

    @Test
    void gainsLifeFromCombatDamage() {
        addCreatureReady(player1, new ShelindaYevonAcolyte());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void putsCounterOnEnteringCreatureWithLessPower() {
        Permanent shelinda = harness.addToBattlefieldAndReturn(player1, new ShelindaYevonAcolyte());

        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent hawk = findPermanent(player1, "Suntail Hawk");
        assertThat(hawk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(shelinda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void putsCounterOnShelindaWhenPowerIsEqual() {
        Permanent shelinda = harness.addToBattlefieldAndReturn(player1, new ShelindaYevonAcolyte());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shelinda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void comparesPowersWhenTriggerResolves() {
        Permanent shelinda = harness.addToBattlefieldAndReturn(player1, new ShelindaYevonAcolyte());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        shelinda.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(shelinda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForItsOwnEntry() {
        Permanent shelinda = harness.enterBattlefieldAndReturn(player1, new ShelindaYevonAcolyte());

        assertThat(gd.stack).isEmpty();
        assertThat(shelinda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForOpponentsCreature() {
        Permanent shelinda = harness.addToBattlefieldAndReturn(player1, new ShelindaYevonAcolyte());
        Permanent hawk = harness.enterBattlefieldAndReturn(player2, new SuntailHawk());

        assertThat(gd.stack).isEmpty();
        assertThat(hawk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(shelinda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void putsCounterOnShelindaWhenEnteringCreatureHasGreaterPower() {
        Permanent shelinda = harness.addToBattlefieldAndReturn(player1, new ShelindaYevonAcolyte());
        shelinda.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(shelinda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(shelinda.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void usesShelindasLastKnownPowerIncludingStaticBonusesAfterSheLeaves() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent shelinda = harness.addToBattlefieldAndReturn(player1, new ShelindaYevonAcolyte());
        Permanent hawk = harness.enterBattlefieldAndReturn(player1, new SuntailHawk());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, shelinda.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Shelinda, Yevon Acolyte");
        harness.assertNotOnBattlefield(player1, "Shelinda, Yevon Acolyte");

        harness.passBothPriorities();

        assertThat(hawk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void usesEnteringCreaturesLastKnownPowerAfterItLeaves() {
        Permanent shelinda = harness.addToBattlefieldAndReturn(player1, new ShelindaYevonAcolyte());
        Permanent hawk = harness.enterBattlefieldAndReturn(player1, new SuntailHawk());
        hawk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, hawk.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Suntail Hawk");
        harness.assertNotOnBattlefield(player1, "Suntail Hawk");

        harness.passBothPriorities();

        assertThat(shelinda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
