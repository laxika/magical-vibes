package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LiltingRefrain.class, CoralMerfolk.class})
class LiltingRefrainTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger puts a verse counter on Lilting Refrain")
    void upkeepAcceptedAddsVerseCounter() {
        Permanent refrain = harness.addToBattlefieldAndReturn(player1, new LiltingRefrain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(refrain.getCounterCount(CounterType.VERSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves verse counters unchanged")
    void upkeepDeclinedAddsNoVerseCounter() {
        Permanent refrain = harness.addToBattlefieldAndReturn(player1, new LiltingRefrain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(refrain.getCounterCount(CounterType.VERSE)).isZero();
    }

    @Test
    @DisplayName("The upkeep trigger does not fire during an opponent's upkeep")
    void upkeepDoesNotTriggerDuringOpponentsUpkeep() {
        Permanent refrain = harness.addToBattlefieldAndReturn(player1, new LiltingRefrain());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(refrain.getCounterCount(CounterType.VERSE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Sacrifice counters a spell unless its controller pays for the verse counters")
    void sacrificeCountersUnlessControllerPaysVerseCounters() {
        Permanent refrain = harness.addToBattlefieldAndReturn(player1, new LiltingRefrain());
        refrain.setCounterCount(CounterType.VERSE, 2);

        CoralMerfolk merfolk = new CoralMerfolk();
        harness.setHand(player2, List.of(merfolk));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Coral Merfolk");
        harness.assertInGraveyard(player1, "Lilting Refrain");
    }

    @Test
    @DisplayName("Sacrifice counters a spell when its controller cannot pay")
    void sacrificeCountersWhenControllerCannotPayVerseCounters() {
        Permanent refrain = harness.addToBattlefieldAndReturn(player1, new LiltingRefrain());
        refrain.setCounterCount(CounterType.VERSE, 2);

        CoralMerfolk merfolk = new CoralMerfolk();
        harness.setHand(player2, List.of(merfolk));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Coral Merfolk");
        harness.assertInGraveyard(player1, "Lilting Refrain");
    }

    @Test
    @DisplayName("Declining the payment counters the targeted spell")
    void sacrificeCountersWhenControllerDeclinesToPay() {
        Permanent refrain = harness.addToBattlefieldAndReturn(player1, new LiltingRefrain());
        refrain.setCounterCount(CounterType.VERSE, 2);

        CoralMerfolk merfolk = new CoralMerfolk();
        harness.setHand(player2, List.of(merfolk));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Coral Merfolk");
        harness.assertInGraveyard(player1, "Lilting Refrain");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("A zero-counter Refrain can be paid for zero mana")
    void zeroVerseCountersCanBePaid() {
        Permanent refrain = harness.addToBattlefieldAndReturn(player1, new LiltingRefrain());

        CoralMerfolk merfolk = new CoralMerfolk();
        harness.setHand(player2, List.of(merfolk));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Coral Merfolk");
        harness.assertInGraveyard(player1, "Lilting Refrain");
    }

    @Test
    @DisplayName("Cannot target a permanent with the activated ability")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new LiltingRefrain());
        harness.addToBattlefield(player2, new CoralMerfolk());

        assertThatThrownBy(() -> harness.activateAbility(
                        player1,
                        0,
                        null,
                        harness.getPermanentId(player2, "Coral Merfolk")
                ))
                .isInstanceOf(IllegalStateException.class);
    }
}
