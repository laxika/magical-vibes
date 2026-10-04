package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.m.MiasmicMummy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaithOfTheDevoted.class, Censor.class, DuneBeetle.class, MiasmicMummy.class})
class FaithOfTheDevotedTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card and paying {1} drains each opponent for 2 and gains you 2")
    void cyclePayDrains() {
        harness.addToBattlefield(player1, new FaithOfTheDevoted());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);      // cycling {U}
        harness.addMana(player1, ManaColor.COLORLESS, 1); // the may-pay {1}

        harness.activateHandAbility(player1, 0, null); // cycle Censor -> discard trigger
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Declining the may-pay leaves life totals unchanged")
    void declineNoDrain() {
        harness.addToBattlefield(player1, new FaithOfTheDevoted());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An ordinary discard triggers once, even when both players discard")
    void ordinaryDiscardTriggersOnlyForController() {
        harness.addToBattlefield(player1, new FaithOfTheDevoted());
        harness.setHand(player1, List.of(new MiasmicMummy(), new DuneBeetle()));
        harness.setHand(player2, List.of(new DuneBeetle()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent cycling does not trigger Faith of the Devoted")
    void opponentCyclingDoesNotTrigger() {
        harness.addToBattlefield(player1, new FaithOfTheDevoted());
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new DuneBeetle()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();

        harness.assertInHand(player2, "Dune Beetle");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling without mana to pay causes neither life loss nor life gain")
    void cannotPayDoesNotDrain() {
        harness.addToBattlefield(player1, new FaithOfTheDevoted());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Dune Beetle");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
