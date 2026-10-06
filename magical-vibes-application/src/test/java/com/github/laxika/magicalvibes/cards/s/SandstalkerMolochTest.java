package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.c.CloudkinSeer;
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
        castOpponentSpell(new CloudkinSeer(), "{2}{U}");

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
        castOpponentSpell(new ChildOfNight(), "{1}{B}");

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

    @Test
    @DisplayName("A permanent may be declined and all four cards go below the untouched library")
    void mayDeclinePermanent() {
        Forest forest = new Forest();
        SandstalkerMoloch creature = new SandstalkerMoloch();
        Shock firstShock = new Shock();
        Shock secondShock = new Shock();
        Forest fifth = new Forest();
        Forest sixth = new Forest();
        setLibrary(List.of(forest, creature, firstShock, secondShock, fifth, sixth));
        castOpponentSpell(new ChildOfNight(), "{1}{B}");

        castMoloch();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2)).containsExactly(fifth, sixth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 6))
                .containsExactlyInAnyOrder(forest, creature, firstShock, secondShock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no eligible permanents the four cards go to the bottom without a choice")
    void noEligiblePermanents() {
        List<Card> topCards = List.of(new Shock(), new Shock(), new Shock(), new Shock());
        Forest fifth = new Forest();
        harness.setLibrary(player1, List.of(topCards.get(0), topCards.get(1),
                topCards.get(2), topCards.get(3), fifth));
        castOpponentSpell(new ChildOfNight(), "{1}{B}");

        castMoloch();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(fifth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrderElementsOf(topCards);
    }

    @Test
    @DisplayName("A library with fewer than four cards still permits taking a creature")
    void shortLibrary() {
        SandstalkerMoloch creature = new SandstalkerMoloch();
        Shock shock = new Shock();
        setLibrary(List.of(shock, creature));
        castOpponentSpell(new ChildOfNight(), "{1}{B}");

        castMoloch();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not require a choice")
    void emptyLibrary() {
        setLibrary(List.of());
        castOpponentSpell(new ChildOfNight(), "{1}{B}");

        castMoloch();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("No ability triggers when no opponent has cast a spell")
    void noOpponentSpell() {
        List<Card> cards = List.of(new Forest(), new SandstalkerMoloch());
        setLibrary(cards);

        harness.castFromHand(player1, new SandstalkerMoloch(), "{1}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(cards);
    }

    @Test
    @DisplayName("The controller's own blue spell does not satisfy the condition")
    void ownBlueSpellDoesNotQualify() {
        harness.castFromHand(player1, new CloudkinSeer(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        List<Card> cards = List.of(new Forest(), new SandstalkerMoloch());
        setLibrary(cards);

        castMoloch();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(cards);
    }

    @Test
    @DisplayName("Flash permits responding to an opponent's blue spell before it resolves")
    void blueSpellNeedNotResolve() {
        Forest forest = new Forest();
        setLibrary(List.of(forest));
        harness.forceActivePlayer(player2);
        CloudkinSeer blueSpell = new CloudkinSeer();
        harness.castFromHand(player2, blueSpell, "{2}{U}");
        harness.castFromHand(player1, new SandstalkerMoloch(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        harness.assertOnBattlefield(player1, "Sandstalker Moloch");
        harness.assertNotOnBattlefield(player2, "Cloudkin Seer");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(blueSpell);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
    }

    private void setLibrary(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void castOpponentSpell(Card spell, String manaCost) {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, manaCost);
        harness.passBothPriorities();
    }

    private void castMoloch() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new SandstalkerMoloch(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
