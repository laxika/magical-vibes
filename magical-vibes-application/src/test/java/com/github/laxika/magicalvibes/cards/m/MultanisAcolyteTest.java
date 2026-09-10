package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MultanisAcolyte.class, MultanisPresence.class})
class MultanisAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void enteringTheBattlefieldDrawsACard() {
        harness.setLibrary(player1, List.of(new MultanisPresence()));
        castAndResolveMultanisAcolyte();

        harness.assertInHand(player1, "Multani's Presence");
    }

    @Test
    @DisplayName("Declining echo sacrifices Multani's Acolyte")
    void decliningEchoSacrificesMultanisAcolyte() {
        castAndResolveMultanisAcolyte();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Multani's Acolyte");
        harness.assertInGraveyard(player1, "Multani's Acolyte");
    }

    @Test
    @DisplayName("Paying echo keeps Multani's Acolyte and echo does not trigger again")
    void payingEchoKeepsMultanisAcolyteAndEchoDoesNotTriggerAgain() {
        castAndResolveMultanisAcolyte();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Multani's Acolyte");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Multani's Acolyte");
    }

    private void castAndResolveMultanisAcolyte() {
        harness.castFromHand(player1, new MultanisAcolyte(), "{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
