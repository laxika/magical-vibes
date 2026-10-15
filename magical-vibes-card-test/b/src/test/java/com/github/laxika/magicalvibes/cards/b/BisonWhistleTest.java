package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AppaLoyalSkyBison;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BisonWhistle.class, AppaLoyalSkyBison.class, GrizzlyBears.class, Forest.class})
class BisonWhistleTest extends BaseCardTest {

    @Test
    @DisplayName("Putting a Bison top card onto the battlefield works")
    void putsBisonOntoBattlefield() {
        AppaLoyalSkyBison appa = new AppaLoyalSkyBison();
        activateWithTopCard(appa);

        harness.handleMayAbilityChosen(player1, true);
        resolveAppaTriggerIfNeeded();

        harness.assertOnBattlefield(player1, "Appa, Loyal Sky Bison");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(appa);
    }

    @Test
    @DisplayName("Declining the Bison placement offers the creature card to hand")
    void declinesBisonPlacementThenPutsCreatureIntoHand() {
        AppaLoyalSkyBison appa = new AppaLoyalSkyBison();
        activateWithTopCard(appa);

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(appa);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(appa);
    }

    @Test
    @DisplayName("A non-Bison creature can be revealed and put into hand")
    void putsCreatureIntoHand() {
        GrizzlyBears bears = new GrizzlyBears();
        activateWithTopCard(bears);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("A noncreature top card may be put into the graveyard")
    void putsNoncreatureIntoGraveyard() {
        Forest forest = new Forest();
        activateWithTopCard(forest);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("Declining a creature card hand choice leaves it on top")
    void declinesCreatureHandChoice() {
        GrizzlyBears bears = new GrizzlyBears();
        activateWithTopCard(bears);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bears);
    }

    private void activateWithTopCard(Card topCard) {
        activateWithLibrary(List.of(topCard));
    }

    @Test
    @DisplayName("Declining both Bison choices leaves the creature on top without a graveyard choice")
    void declinesBothBisonChoices() {
        AppaLoyalSkyBison appa = new AppaLoyalSkyBison();
        activateWithTopCard(appa);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(appa);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(appa);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(appa);
        harness.assertNotOnBattlefield(player1, "Appa, Loyal Sky Bison");
    }

    @Test
    @DisplayName("Declining the graveyard choice leaves a noncreature on top")
    void declinesNoncreatureGraveyardChoice() {
        Forest forest = new Forest();
        activateWithTopCard(forest);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("An empty library resolves without offering a choice")
    void emptyLibraryDoesNothing() {
        activateWithLibrary(List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    private void activateWithLibrary(List<Card> cards) {
        Permanent whistle = harness.addToBattlefieldAndReturn(player1, new BisonWhistle());
        whistle.setSummoningSick(false);
        harness.setLibrary(player1, cards);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void resolveAppaTriggerIfNeeded() {
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.ColorChoice) {
            harness.handleListChoice(player1, "Target creature you control gains flying until end of turn");
            harness.handlePermanentChosen(player1,
                    harness.getPermanentId(player1, "Appa, Loyal Sky Bison"));
            harness.passBothPriorities();
        }
    }
}
