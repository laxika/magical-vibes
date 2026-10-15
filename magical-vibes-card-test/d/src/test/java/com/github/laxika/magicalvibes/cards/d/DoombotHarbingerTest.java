package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoombotHarbinger.class, Forest.class, GrizzlyBears.class, WrathOfGod.class})
class DoombotHarbingerTest extends BaseCardTest {

    @Test
    void mayMillFourCardsWhenItEnters() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.castFromHand(player1, new DoombotHarbinger(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(first.getId(), second.getId(), third.getId(), fourth.getId());
    }

    @Test
    void mayExileItselfAndReturnTargetCreatureCardToHand() {
        Card doombot = addDoombotToBattlefield();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        destroyDoombot();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(target.getId()).doesNotContain(doombot.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(doombot.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(target.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(target.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(doombot.getId());
    }

    @Test
    void decliningSelfExileLeavesSourceAndTargetInGraveyard() {
        Card doombot = addDoombotToBattlefield();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        destroyDoombot();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(doombot.getId(), target.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(target.getId());
    }

    @Test
    void mayDeclineToMill() {
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));
        harness.castFromHand(player1, new DoombotHarbinger(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void millsOnlyAvailableCardsFromItsControllersLibrary() {
        Card first = new Forest();
        Card second = new Forest();
        Card opposingCard = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(opposingCard));
        harness.castFromHand(player1, new DoombotHarbinger(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void mayExileItselfEvenWhenNoOtherCreatureCanBeReturned() {
        Card doombot = addDoombotToBattlefield();
        destroyDoombot();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(doombot.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(doombot.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(doombot.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnTriggerTargetsOnlyCreatureCardsInItsControllersGraveyard() {
        Card doombot = addDoombotToBattlefield();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setGraveyard(player2, List.of(opposingCreature));
        destroyDoombot();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(doombot);
    }

    @Test
    void acceptingMillWithAnEmptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new DoombotHarbinger(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Doombot Harbinger");
    }

    private Card addDoombotToBattlefield() {
        Card doombot = new DoombotHarbinger();
        harness.addToBattlefield(player1, doombot);
        return doombot;
    }

    private void destroyDoombot() {
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
    }
}
