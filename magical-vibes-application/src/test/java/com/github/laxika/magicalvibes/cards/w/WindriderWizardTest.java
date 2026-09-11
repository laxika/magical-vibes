package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WindriderWizard.class, Forest.class, FugitiveWizard.class, GrizzlyBears.class, Shock.class})
class WindriderWizardTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger draws a card, then prompts for a discard")
    void acceptingTriggerDrawsThenDiscards() {
        harness.addToBattlefield(player1, new WindriderWizard());
        harness.setHand(player1, List.of(new Shock(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Declining the trigger does not draw or discard")
    void decliningTriggerDoesNothing() {
        harness.addToBattlefield(player1, new WindriderWizard());
        harness.setHand(player1, List.of(new Shock(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).extracting(com.github.laxika.magicalvibes.model.Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(com.github.laxika.magicalvibes.model.Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("A non-Wizard creature spell does not trigger the ability")
    void nonWizardCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new WindriderWizard());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A Wizard creature spell triggers the ability")
    void wizardCreatureTriggers() {
        harness.addToBattlefield(player1, new WindriderWizard());
        harness.setHand(player1, List.of(new FugitiveWizard(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }
}
