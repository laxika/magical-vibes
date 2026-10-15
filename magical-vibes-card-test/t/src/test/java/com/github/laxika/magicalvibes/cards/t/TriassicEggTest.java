package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AcidRain;
import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TriassicEgg.class, BarbaryApes.class, AcidRain.class})
class TriassicEggTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability puts a hatchling counter on Triassic Egg")
    void putsHatchlingCounterOnEgg() {
        Permanent egg = addCreatureReady(player1, new TriassicEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.HATCHLING)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability requires two hatchling counters")
    void reanimationAbilitiesRequireTwoHatchlingCounters() {
        Permanent egg = addCreatureReady(player1, new TriassicEgg());
        egg.setCounterCount(CounterType.HATCHLING, 1);
        Card creature = new BarbaryApes();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The hand mode sacrifices the Egg and may puts a creature from hand onto the battlefield")
    void handModePutsCreatureFromHandOntoBattlefield() {
        Permanent egg = addCreatureReady(player1, new TriassicEgg());
        egg.setCounterCount(CounterType.HATCHLING, 2);
        Card creature = new BarbaryApes();
        harness.setHand(player1, List.of(creature, new AcidRain()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Triassic Egg");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Barbary Apes");
        harness.assertNotInGraveyard(player1, "Barbary Apes");
    }

    @Test
    @DisplayName("The hand mode may be declined after sacrificing the Egg")
    void handModeMayBeDeclinedAfterSacrifice() {
        Permanent egg = addCreatureReady(player1, new TriassicEgg());
        egg.setCounterCount(CounterType.HATCHLING, 2);
        Card creature = new BarbaryApes();
        harness.setHand(player1, List.of(creature));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Triassic Egg");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Barbary Apes");
        harness.assertInHand(player1, "Barbary Apes");
    }

    @Test
    @DisplayName("The graveyard mode sacrifices the Egg and returns a targeted creature")
    void graveyardModeReturnsTargetCreature() {
        Permanent egg = addCreatureReady(player1, new TriassicEgg());
        egg.setCounterCount(CounterType.HATCHLING, 2);
        Card creature = new BarbaryApes();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, 2, null, creature.getId(), Zone.GRAVEYARD);
        harness.assertInGraveyard(player1, "Triassic Egg");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Barbary Apes");
        harness.assertNotInGraveyard(player1, "Barbary Apes");
    }

    @Test
    @DisplayName("The graveyard mode only targets creature cards in your graveyard")
    void graveyardModeRejectsNonCreatureTarget() {
        Permanent egg = addCreatureReady(player1, new TriassicEgg());
        egg.setCounterCount(CounterType.HATCHLING, 2);
        Card nonCreature = new AcidRain();
        harness.setGraveyard(player1, List.of(nonCreature));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 2, null, nonCreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Triassic Egg");
        harness.assertInGraveyard(player1, "Acid Rain");
    }

    @Test
    @DisplayName("The counter ability taps the Egg before putting a counter on resolution")
    void counterAbilityPaysTapCostBeforeResolution() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new TriassicEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(egg.isTapped()).isTrue();
        assertThat(egg.getCounterCount(CounterType.HATCHLING)).isZero();

        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.HATCHLING)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter ability cannot be activated without three mana")
    void counterAbilityRequiresThreeMana() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new TriassicEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(egg.isTapped()).isFalse();
        assertThat(egg.getCounterCount(CounterType.HATCHLING)).isZero();
    }

    @Test
    @DisplayName("The counter ability cannot be activated while the Egg is tapped")
    void counterAbilityRequiresUntappedEgg() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new TriassicEgg());
        egg.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(egg.getCounterCount(CounterType.HATCHLING)).isZero();
    }

    @Test
    @DisplayName("The hand mode can sacrifice a tapped Egg with more than two counters")
    void handModeAcceptsTappedEggWithExtraCounters() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new TriassicEgg());
        egg.setCounterCount(CounterType.HATCHLING, 3);
        egg.tap();
        harness.setHand(player1, List.of(new BarbaryApes()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Triassic Egg");
        harness.assertOnBattlefield(player1, "Barbary Apes");
        harness.assertNotInHand(player1, "Barbary Apes");
    }

    @Test
    @DisplayName("The graveyard mode can sacrifice a tapped Egg with more than two counters")
    void graveyardModeAcceptsTappedEggWithExtraCounters() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new TriassicEgg());
        egg.setCounterCount(CounterType.HATCHLING, 3);
        egg.tap();
        Card creature = new BarbaryApes();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, 2, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Triassic Egg");
        harness.assertOnBattlefield(player1, "Barbary Apes");
    }

    @Test
    @DisplayName("The hand mode can be activated without a creature in hand")
    void handModeWithNoCreatureInHandDoesNothing() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new TriassicEgg());
        egg.setCounterCount(CounterType.HATCHLING, 2);
        harness.setHand(player1, List.of(new AcidRain()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Triassic Egg");
        harness.assertInHand(player1, "Acid Rain");
        harness.assertNotOnBattlefield(player1, "Acid Rain");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The graveyard mode cannot target an opponent's creature card")
    void graveyardModeRejectsOpponentGraveyard() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new TriassicEgg());
        egg.setCounterCount(CounterType.HATCHLING, 2);
        Card creature = new BarbaryApes();
        harness.setGraveyard(player2, List.of(creature));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Triassic Egg");
        harness.assertInGraveyard(player2, "Barbary Apes");
    }

    @Test
    @DisplayName("The graveyard mode requires a target before the Egg is sacrificed")
    void graveyardModeRequiresTarget() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new TriassicEgg());
        egg.setCounterCount(CounterType.HATCHLING, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null, Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Triassic Egg");
        harness.assertNotInGraveyard(player1, "Triassic Egg");
    }

    @Test
    @DisplayName("The graveyard mode does not return a target that leaves the graveyard")
    void graveyardModeLosesTargetBeforeResolution() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new TriassicEgg());
        egg.setCounterCount(CounterType.HATCHLING, 2);
        Card creature = new BarbaryApes();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, 2, null, creature.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of(egg.getCard()));
        harness.setHand(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Triassic Egg");
        harness.assertInHand(player1, "Barbary Apes");
        harness.assertNotOnBattlefield(player1, "Barbary Apes");
    }
}
