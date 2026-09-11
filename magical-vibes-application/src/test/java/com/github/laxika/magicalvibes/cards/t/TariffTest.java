package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tariff.class, GrizzlyBears.class, GiantSpider.class})
class TariffTest extends BaseCardTest {

    @Test
    @DisplayName("Paying the creature's mana cost keeps it")
    void payingKeepsCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromHand(player1, new Tariff(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining to pay sacrifices the creature")
    void decliningSacrificesCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromHand(player1, new Tariff(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A player who can't pay sacrifices the creature with no prompt")
    void cantPayAutoSacrifices() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castFromHand(player1, new Tariff(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Each player is affected in turn order")
    void eachPlayerAffected() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castFromHand(player1, new Tariff(), "{1}{W}");
        harness.passBothPriorities();

        // Active player (player1) is prompted first.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        // Then player2.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("With a tie for greatest mana value, the player chooses which creature is at risk")
    void tieBreakChoosesCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new Tariff(), "{1}{W}");
        harness.passBothPriorities();

        // Tied at mana value 2 — player1 chooses which Grizzly Bears is at risk.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, first.getId());

        // The chosen one is sacrificed (can't pay); the other survives.
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .noneMatch(p -> p.getId().equals(first.getId()))
                .hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("It selects the creature with greatest mana value")
    void selectsGreatestManaValueCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GiantSpider());

        harness.castFromHand(player1, new Tariff(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("A player with no creatures is not prompted")
    void noCreaturesNeedsNoChoice() {
        harness.castFromHand(player1, new Tariff(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Tariff");
    }
}
