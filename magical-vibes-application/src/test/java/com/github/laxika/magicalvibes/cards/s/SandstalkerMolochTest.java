package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.c.CloudkinSeer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SandstalkerMoloch.class, CloudkinSeer.class, ChildOfNight.class,
        GrizzlyBears.class, Forest.class, Shock.class})
class SandstalkerMolochTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a permanent from the top four after an opponent casts a blue spell")
    void offersPermanentAfterBlueSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        Shock shock = new Shock();
        Shock secondShock = new Shock();
        setLibrary(List.of(bears, shock, forest, secondShock));
        castOpponentSpell(new CloudkinSeer(), ManaColor.BLUE, 4);

        castMoloch();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId(), forest.getId());

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bears, shock, secondShock);
    }

    @Test
    @DisplayName("ETB offers a permanent after an opponent casts a black spell")
    void offersPermanentAfterBlackSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(List.of(new Shock(), bears, new Shock(), new Forest()));
        castOpponentSpell(new ChildOfNight(), ManaColor.BLACK, 2);

        castMoloch();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    @DisplayName("ETB does not trigger after an opponent casts only a red spell")
    void doesNotTriggerAfterRedSpell() {
        List<Card> topCards = List.of(new Forest(), new Shock(), new GrizzlyBears(), new Shock());
        setLibrary(topCards);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        castMoloch();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(topCards);
    }

    private void setLibrary(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }

    private void castOpponentSpell(Card spell, ManaColor manaColor, int manaValue) {
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, manaColor, manaValue);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
    }

    private void castMoloch() {
        harness.setHand(player1, List.of(new SandstalkerMoloch()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
