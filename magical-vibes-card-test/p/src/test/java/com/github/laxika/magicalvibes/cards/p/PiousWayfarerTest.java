package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PiousWayfarer.class, NyxbornCourser.class})
class PiousWayfarerTest extends BaseCardTest {

    @Test
    @DisplayName("Pious Wayfarer's own entry does not trigger constellation")
    void ownEntryDoesNotTrigger() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.castFromHand(player1, new PiousWayfarer(), "{W}");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Boosts a target creature when an enchantment you control enters")
    void boostsTargetCreatureWhenOwnEnchantmentEnters() {
        harness.addToBattlefield(player1, new PiousWayfarer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.castFromHand(player1, new NyxbornCourser(), "{1}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for an enchantment an opponent controls")
    void opponentEnchantmentDoesNotTrigger() {
        PiousWayfarer wayfarer = new PiousWayfarer();
        harness.addToBattlefield(player1, wayfarer);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new NyxbornCourser(), "{1}{W}{W}");
        resolveAllTriggers();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Constellation can target an opponent's creature")
    void canBoostOpponentCreature() {
        harness.addToBattlefield(player1, new PiousWayfarer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.castFromHand(player1, new NyxbornCourser(), "{1}{W}{W}");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Constellation can target the enchantment creature that entered")
    void canBoostEnteringEnchantmentCreature() {
        harness.addToBattlefield(player1, new PiousWayfarer());
        harness.castFromHand(player1, new NyxbornCourser(), "{1}{W}{W}");
        resolveAllTriggers();

        Permanent courser = findPermanent(player1, "Nyxborn Courser");
        harness.handlePermanentChosen(player1, courser.getId());
        resolveAllTriggers();

        assertThat(courser.getPowerModifier()).isEqualTo(1);
        assertThat(courser.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Constellation can boost Pious Wayfarer and expires at end of turn")
    void canBoostItselfUntilEndOfTurn() {
        Permanent wayfarer = harness.addToBattlefieldAndReturn(player1, new PiousWayfarer());
        harness.castFromHand(player1, new NyxbornCourser(), "{1}{W}{W}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, wayfarer.getId());
        resolveAllTriggers();

        assertThat(wayfarer.getPowerModifier()).isEqualTo(1);
        assertThat(wayfarer.getToughnessModifier()).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(wayfarer.getPowerModifier()).isZero();
        assertThat(wayfarer.getToughnessModifier()).isZero();
    }
}
