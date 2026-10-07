package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.r.RiverBoa;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurntimberRanger.class, RiverBoa.class, IntoTheRoil.class, Conspiracy.class})
class TurntimberRangerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting its Ally-entry trigger creates a Wolf and adds a counter")
    void acceptingTriggerCreatesWolfAndAddsCounter() {
        harness.setHand(player1, List.of(new TurntimberRanger()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent ranger = findPermanent(player1, "Turntimber Ranger");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        Permanent wolf = findPermanent(player1, "Wolf");
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
        assertThat(wolf.getEffectivePower()).isEqualTo(2);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(2);
        assertThat(ranger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining its Ally-entry trigger creates no Wolf or counter")
    void decliningTriggerDoesNothing() {
        harness.setHand(player1, List.of(new TurntimberRanger()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(findPermanent(player1, "Turntimber Ranger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger Turntimber Ranger")
    void nonAllyEntryDoesNotTrigger() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new TurntimberRanger());
        harness.setHand(player1, List.of(new RiverBoa()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(ranger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void anotherAllyTriggersEachRangerOnceAndWolvesDoNotRetrigger() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TurntimberRanger());
        harness.setHand(player1, List.of(new TurntimberRanger()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(2);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Turntimber Ranger"))
                .allSatisfy(ranger -> assertThat(ranger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(1));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentsAllyDoesNotTriggerOwnRanger() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new TurntimberRanger());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new TurntimberRanger()));
        harness.addMana(player2, ManaColor.GREEN, 5);

        harness.castCreature(player2, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Wolf")).isEqualTo(1);
        assertThat(countPermanents(player1, "Wolf")).isZero();
        assertThat(ranger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void triggerStillCreatesWolfAfterRangerReturnsToHand() {
        harness.setHand(player1, List.of(new TurntimberRanger(), new IntoTheRoil()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent ranger = findPermanent(player1, "Turntimber Ranger");

        harness.castInstant(player1, 0, ranger.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Turntimber Ranger");
        harness.assertNotOnBattlefield(player1, "Turntimber Ranger");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void selfEntryTriggersEvenWhenItsCreatureTypesAreReplaced() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        harness.setHand(player1, List.of(new TurntimberRanger()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
        assertThat(findPermanent(player1, "Turntimber Ranger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
