package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DreadwingScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card, then discards a card")
    void entersDrawsThenDiscards() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, new ArrayList<>(List.of(new DreadwingScavenger(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getName().equals("Island"));

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Attacking draws a card, then discards a card")
    void attacksDrawsThenDiscards() {
        addCreatureReady(player1, new DreadwingScavenger());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, new ArrayList<>(List.of(new Forest())));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Threshold grants +1/+1 and deathtouch")
    void thresholdGrantsBoostAndDeathtouch() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent scavenger = addCreatureReady(player1, new DreadwingScavenger());

        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scavenger)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, scavenger, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Threshold does not apply with fewer than seven cards in its controller's graveyard")
    void thresholdDoesNotApplyBelowSevenCards() {
        harness.setGraveyard(player1, graveyardCards(6));
        Permanent scavenger = addCreatureReady(player1, new DreadwingScavenger());

        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scavenger)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, scavenger, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Threshold counts only its controller's graveyard")
    void opponentGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player2, graveyardCards(7));
        Permanent scavenger = addCreatureReady(player1, new DreadwingScavenger());

        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, scavenger, Keyword.DEATHTOUCH)).isFalse();
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Spellbook());
        }
        return cards;
    }
}
