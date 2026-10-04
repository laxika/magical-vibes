package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.s.SnappingCreeper;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraypeltHunter.class, SnappingCreeper.class, Conspiracy.class})
class GraypeltHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may put a +1/+1 counter on it")
    void ownAllyEntryMayPutCounterOnIt() {
        harness.setHand(player1, List.of(new GraypeltHunter()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent hunter = findPermanent(player1, "Graypelt Hunter");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An Ally entry triggers both the existing and entering Hunters")
    void anotherAllyEntryTriggersBothHunters() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GraypeltHunter());
        harness.setHand(player1, List.of(new GraypeltHunter()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent entering = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new GraypeltHunter());
        harness.setHand(player1, List.of(new SnappingCreeper()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability does not add a counter")
    void mayBeDeclined() {
        harness.setHand(player1, List.of(new GraypeltHunter()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Graypelt Hunter")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsAllyDoesNotTriggerHunter() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new GraypeltHunter());
        Permanent opponentHunter = harness.enterBattlefieldAndReturn(player2, new GraypeltHunter());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentHunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void ownEntryStillTriggersWhenItsCreatureTypeIsReplaced() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        harness.setHand(player1, List.of(new GraypeltHunter()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(findPermanent(player1, "Graypelt Hunter")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void trampleDealsExcessDamageAfterItsEntryCounter() {
        harness.setHand(player1, List.of(new GraypeltHunter()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        findPermanent(player1, "Graypelt Hunter").setSummoningSick(false);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GraypeltHunter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Graypelt Hunter");
        assertThat(findPermanent(player1, "Graypelt Hunter")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
