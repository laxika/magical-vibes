package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbundantMaw.class, GrizzlyBears.class})
class AbundantMawTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: when cast, target opponent loses 3 life and controller gains 3")
    void hardcastDrainsOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AbundantMaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        // Choose opponent for the ON_SELF_CAST trigger
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities(); // resolve cast trigger
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertOnBattlefield(player1, "Abundant Maw");
    }

    @Test
    @DisplayName("Emerge: sacrifice a creature, pay emerge cost reduced by its mana value")
    void emergeSacrificesAndReducesCost() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AbundantMaw()));
        // Emerge {6}{B} reduced by 2 → {4}{B}
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertOnBattlefield(player1, "Abundant Maw");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Emerge fails without enough mana after reduction")
    void emergeFailsWithInsufficientMana() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new AbundantMaw()));
        // Need {4}{B} after reduction; only {3}{B} available
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cast trigger cannot target the controller")
    void castTriggerCannotTargetSelf() {
        harness.setHand(player1, List.of(new AbundantMaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cast trigger resolves even if the creature spell is still on the stack")
    void castTriggerResolvesBeforeCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AbundantMaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());

        // Trigger sits above the creature spell
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve trigger only

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertNotOnBattlefield(player1, "Abundant Maw");
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Abundant Maw");
    }

    @Test
    @DisplayName("Emerge reduction cannot remove the black mana requirement")
    void emergeWithHighManaValueCreatureStillRequiresBlack() {
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, new AbundantMaw()).getId();
        harness.setHand(player1, List.of(new AbundantMaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificeId)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Abundant Maw");
        harness.assertInHand(player1, "Abundant Maw");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Emerge can reduce the generic cost to zero")
    void emergeWithHighManaValueCreatureCostsOnlyBlack() {
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, new AbundantMaw()).getId();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AbundantMaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificeId));

        harness.assertInGraveyard(player1, "Abundant Maw");
        harness.assertNotOnBattlefield(player1, "Abundant Maw");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "Abundant Maw");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Entering without being cast does not drain life")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player1, new AbundantMaw());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Abundant Maw");
    }

    @Test
    @DisplayName("Emerge cannot sacrifice an opponent's creature")
    void emergeCannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new AbundantMaw());
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player2, new AbundantMaw()).getId();
        harness.setHand(player1, List.of(new AbundantMaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificeId)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Abundant Maw");
        harness.assertInHand(player1, "Abundant Maw");
        assertThat(gd.stack).isEmpty();
    }
}
