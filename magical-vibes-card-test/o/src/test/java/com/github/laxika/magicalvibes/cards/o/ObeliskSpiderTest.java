package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObeliskSpider.class, AirElemental.class, GrizzlyBears.class, HillGiant.class, Skinrender.class})
class ObeliskSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a creature puts a -1/-1 counter and drains 1/gains 1")
    void combatDamagePutsCounterAndDrains() {
        Permanent spider = addCreatureReady(player1, new ObeliskSpider());
        spider.setAttacking(true);
        // 3/3: survives 1 combat damage + one -1/-1; deals only 3 so the 1/4 spider also lives.
        addCreatureReady(player2, new HillGiant());

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before + 1);
    }

    @Test
    @DisplayName("No drain when dealing combat damage to a player (no creature damaged)")
    void noTriggerOnDamageToPlayer() {
        Permanent spider = addCreatureReady(player1, new ObeliskSpider());
        spider.setAttacking(true);

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1); // combat damage only
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before);
    }

    @Test
    @DisplayName("Putting multiple -1/-1 counters at once drains only once")
    void multipleCountersAtOnceDrainOnce() {
        harness.addToBattlefield(player1, new ObeliskSpider());
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        Permanent airElemental = findPermanent(player2, "Air Elemental");
        assertThat(airElemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before + 1);
    }

    @Test
    @DisplayName("An opponent putting the -1/-1 counters does not trigger your Obelisk Spider")
    void opponentPlacingCountersDoesNotTrigger() {
        harness.addToBattlefield(player1, new ObeliskSpider());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player2, List.of(new Skinrender()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0, targetId);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before);
    }

    @Test
    @DisplayName("A Spider that dies in combat still puts a counter but does not drain")
    void dyingSpiderStillPutsCounterWithoutDraining() {
        Permanent attacker = addCreatureReady(player2, new AirElemental());
        attacker.setAttacking(true);
        addCreatureReady(player1, new ObeliskSpider());
        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Obelisk Spider")).isEmpty();
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before);
    }

    @Test
    @DisplayName("A counter can make combat damage lethal and still trigger life drain")
    void counterMakesCombatDamageLethal() {
        Permanent spider = addCreatureReady(player1, new ObeliskSpider());
        spider.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1);
    }

    @Test
    @DisplayName("Putting counters on the Spider itself triggers its life drain once")
    void countersOnSpiderItselfDrainOnce() {
        harness.addToBattlefield(player1, new ObeliskSpider());
        UUID spiderId = harness.getPermanentId(player1, "Obelisk Spider");
        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0, spiderId);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Obelisk Spider")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1);
    }

    @Test
    @DisplayName("No counter or life drain when the damaged creature dies from combat damage")
    void creatureAlreadyDeadDoesNotCauseDrain() {
        Permanent spider = addCreatureReady(player1, new ObeliskSpider());
        spider.setAttacking(true);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setMarkedDamage(1);
        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before);
    }

    @Test
    @DisplayName("Life drain triggers even when the counter cancels a +1/+1 counter")
    void cancellingCountersStillTriggersDrain() {
        Permanent spider = addCreatureReady(player1, new ObeliskSpider());
        spider.setAttacking(true);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1);
    }

    @Test
    @DisplayName("Each Spider drains when your counters kill a creature")
    void eachSpiderTriggersForCountersOnDyingCreature() {
        harness.addToBattlefield(player1, new ObeliskSpider());
        harness.addToBattlefield(player1, new ObeliskSpider());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 2);
    }
}
