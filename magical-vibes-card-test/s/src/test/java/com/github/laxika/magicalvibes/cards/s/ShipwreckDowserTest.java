package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShipwreckDowser.class, Shock.class, Divination.class, GrizzlyBears.class})
class ShipwreckDowserTest extends BaseCardTest {

    private void castShipwreckDowser() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ShipwreckDowser()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addShipwreckDowser() {
        Permanent dowser = harness.addToBattlefieldAndReturn(player1, new ShipwreckDowser());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return dowser;
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted instant card from graveyard to hand")
    void etbReturnsInstantToHand() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        castShipwreckDowser();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("ETB returns a targeted sorcery card from graveyard to hand")
    void etbReturnsSorceryToHand() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));

        castShipwreckDowser();

        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Divination");
        harness.assertNotInGraveyard(player1, "Divination");
    }

    @Test
    @DisplayName("A creature card in the graveyard is not a legal target")
    void creatureNotTargetable() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castShipwreckDowser();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent dowser = addShipwreckDowser();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(1);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, dowser)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dowser)).isEqualTo(4);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, dowser)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dowser)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger prowess")
    void creatureSpellDoesNotPump() {
        Permanent dowser = addShipwreckDowser();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, dowser)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dowser)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB cannot return an instant from an opponent's graveyard")
    void opponentGraveyardIsNotTargetable() {
        harness.setGraveyard(player2, List.of(new Shock()));

        castShipwreckDowser();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Shock");
        harness.assertNotInHand(player1, "Shock");
        harness.assertOnBattlefield(player1, "Shipwreck Dowser");
    }

    @Test
    @DisplayName("ETB with an empty graveyard does not require a choice")
    void emptyGraveyardNeedsNoChoice() {
        castShipwreckDowser();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Shipwreck Dowser");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB does not return another card when its chosen target leaves the graveyard")
    void removedTargetIsNotReplaced() {
        Shock target = new Shock();
        Shock other = new Shock();
        harness.setGraveyard(player1, List.of(target, other));
        castShipwreckDowser();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentSpellDoesNotPump() {
        Permanent dowser = addShipwreckDowser();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, dowser)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dowser)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each noncreature spell adds a separate prowess boost before the spell resolves")
    void prowessBoostsAccumulate() {
        Permanent dowser = addShipwreckDowser();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, dowser)).isEqualTo(4);
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, dowser)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dowser)).isEqualTo(5);

        endTurn();
        assertThat(gqs.getEffectivePower(gd, dowser)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dowser)).isEqualTo(3);
    }
}
