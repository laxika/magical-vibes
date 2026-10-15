package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WindriderWizard.class, Forest.class, FugitiveWizard.class, GrizzlyBears.class, Shock.class, LavaAxe.class})
class WindriderWizardTest extends BaseCardTest {

    @Test
    @DisplayName("The optional draw is chosen when the triggered ability resolves")
    void optionalDrawChoiceWaitsForResolution() {
        harness.addToBattlefield(player1, new WindriderWizard());
        harness.setHand(player1, List.of(new Shock(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        resolveAllTriggers();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A sorcery triggers looting before the spell resolves")
    void sorceryTriggersBeforeResolving() {
        harness.addToBattlefield(player1, new WindriderWizard());
        harness.setHand(player1, List.of(new LavaAxe(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");

        resolveAllTriggers();
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("The drawn card can be discarded when casting the last card in hand")
    void lastCardInHandStillAllowsLooting() {
        harness.addToBattlefield(player1, new WindriderWizard());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's instant does not trigger the ability")
    void opponentsInstantDoesNotTrigger() {
        harness.addToBattlefield(player1, new WindriderWizard());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 18);
    }

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
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }
}
