package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ovinize;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.cards.s.SplendidAgony;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HapatraVizierOfPoisons.class, AirElemental.class, GrizzlyBears.class,
        Skinrender.class, Colossapede.class, SplendidAgony.class, Ovinize.class})
class HapatraVizierOfPoisonsTest extends BaseCardTest {

    private long snakeCount(Player player) {
        return countPermanents(player, "Snake");
    }

    private Permanent attackWithHapatra(Player player) {
        Permanent hapatra = addCreatureReady(player, new HapatraVizierOfPoisons());
        hapatra.setAttacking(true);
        return hapatra;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities(); // resolve what combat damage triggered
    }

    @Test
    @DisplayName("Combat damage: may put a -1/-1 counter on target creature, which makes a Snake")
    void combatDamagePutsCounterAndCreatesSnake() {
        attackWithHapatra(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        resolveCombatAndTrigger();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        // The -1/-1 counter placement triggers the second ability once: one Snake for Hapatra's controller.
        assertThat(snakeCount(player1)).isEqualTo(1);
        assertThat(snakeCount(player2)).isZero();
    }

    @Test
    @DisplayName("Declining the combat trigger places no counter and makes no Snake")
    void decliningCombatTriggerDoesNothing() {
        attackWithHapatra(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        resolveCombatAndTrigger();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(snakeCount(player1)).isZero();
    }

    @Test
    @DisplayName("Multiple -1/-1 counters on one creature at once make only a single Snake")
    void multipleCountersAtOnceMakeOneSnake() {
        harness.addToBattlefield(player1, new HapatraVizierOfPoisons());
        // 4/4 survives three -1/-1 counters (becomes 1/1), so no death interferes.
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        // player1 casts Skinrender → player1 puts three -1/-1 counters in one instance.
        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        Permanent airElemental = findPermanent(player2, "Air Elemental");
        assertThat(airElemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        // Once per creature per instance — not once per counter.
        assertThat(snakeCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent putting the -1/-1 counters does not trigger your Hapatra")
    void opponentPlacingCountersDoesNotTrigger() {
        harness.addToBattlefield(player1, new HapatraVizierOfPoisons());
        // The creature receiving the counters belongs to player1 so player2's Skinrender has a target.
        harness.addToBattlefield(player1, new AirElemental());
        UUID targetId = harness.getPermanentId(player1, "Air Elemental");

        harness.setHand(player2, List.of(new Skinrender()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0, targetId);
        resolveAllTriggers();

        assertThat(snakeCount(player1)).isZero();
    }

    @Test
    void countersOnTwoCreaturesCreateTwoSnakes() {
        harness.addToBattlefield(player1, new HapatraVizierOfPoisons());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        harness.setHand(player1, List.of(new SplendidAgony()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        resolveAllTriggers();

        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(snakeCount(player1)).isEqualTo(2);
        assertThat(snakeCount(player2)).isZero();
    }

    @Test
    void lethalCountersOnHapatraStillCreateOneSnake() {
        Permanent hapatra = harness.addToBattlefieldAndReturn(player1, new HapatraVizierOfPoisons());
        harness.setHand(player1, List.of(new SplendidAgony()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, List.of(hapatra.getId()));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Hapatra, Vizier of Poisons")).isZero();
        assertThat(snakeCount(player1)).isEqualTo(1);
    }

    @Test
    void losingAbilitiesPreventsCounterPlacementTrigger() {
        Permanent hapatra = harness.addToBattlefieldAndReturn(player1, new HapatraVizierOfPoisons());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        harness.setHand(player1, List.of(new Ovinize(), new SplendidAgony()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, hapatra.getId());
        harness.castAndResolveInstant(player1, 0, List.of(creature.getId()));
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(snakeCount(player1)).isZero();
    }
}
