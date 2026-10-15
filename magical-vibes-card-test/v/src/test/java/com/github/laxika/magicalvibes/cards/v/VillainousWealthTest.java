package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.b.BeanstalkGiant;
import com.github.laxika.magicalvibes.cards.f.FertileFootsteps;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VillainousWealth.class, Cancel.class, Forest.class, GrizzlyBears.class, Shock.class,
        AlpineGrizzly.class, ValleyDasher.class, BeanstalkGiant.class, FertileFootsteps.class})
class VillainousWealthTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top X cards of the target opponent's library")
    void exilesTopXCardsOfTargetOpponentLibrary() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(first, second, third, fourth));

        cast(3, player2.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(fourth);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second, third);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Offers nonland spells with mana value X or less from the exiled cards")
    void offersCastableSpellsAtOrBelowX() {
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        Cancel cancel = new Cancel();
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(shock, bears, cancel, forest));

        cast(3, player2.getId());

        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).containsExactlyInAnyOrder(
                shock.getId(), bears.getId(), cancel.getId());
        assertThat(interaction.validCardIds()).doesNotContain(forest.getId());
    }

    @Test
    @DisplayName("Chosen exiled card is cast for free and unchosen cards remain exiled")
    void castsChosenCardForFreeAndLeavesUnchosenCardsExiled() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(bears, forest));

        cast(2, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("An opponent is required as the target")
    void requiresOpponentTarget() {
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new VillainousWealth()));
        addMana(1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroExilesNothing() {
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(forest));

        cast(0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Villainous Wealth");
    }

    @Test
    void shortLibraryExilesOnlyAvailableCards() {
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(forest));

        cast(3, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canDeclineAllSpells() {
        ValleyDasher dasher = new ValleyDasher();
        harness.setLibrary(player2, List.of(dasher));

        cast(2, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(dasher);
        harness.assertNotOnBattlefield(player1, "Valley Dasher");
        harness.assertInGraveyard(player1, "Villainous Wealth");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotOfferSpellsAboveXOrPreviouslyExiledCards() {
        AlpineGrizzly grizzly = new AlpineGrizzly();
        ValleyDasher dasher = new ValleyDasher();
        ValleyDasher previouslyExiled = new ValleyDasher();
        harness.setExile(player2, List.of(previouslyExiled));
        harness.setLibrary(player2, List.of(grizzly, dasher));

        cast(2, player2.getId());

        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).containsExactly(dasher.getId());
    }

    @Test
    void castsMultipleCreaturesUnderCasterControl() {
        AlpineGrizzly grizzly = new AlpineGrizzly();
        ValleyDasher dasher = new ValleyDasher();
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(grizzly, dasher, forest));

        cast(3, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(grizzly.getId(), dasher.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Alpine Grizzly");
        harness.assertOnBattlefield(player1, "Valley Dasher");
        harness.assertNotOnBattlefield(player2, "Alpine Grizzly");
        harness.assertNotOnBattlefield(player2, "Valley Dasher");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(forest);
    }

    @Test
    void offersAdventureWhoseManaValueIsWithinX() {
        BeanstalkGiant giant = new BeanstalkGiant();
        harness.setLibrary(player2, List.of(giant, new Forest(), new Forest()));

        cast(3, player2.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).contains(giant.getId());
    }

    private void cast(int xValue, java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new VillainousWealth()));
        addMana(xValue);
        harness.castAndResolveSorcery(player1, 0, xValue, targetPlayerId);
    }

    private void addMana(int xValue) {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}
