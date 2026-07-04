package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseNToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrackersInstinctsTest extends BaseCardTest {

    @Test
    @DisplayName("Tracker's Instincts has correct effect and flashback cost")
    void hasCorrectProperties() {
        TrackersInstincts card = new TrackersInstincts();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(LookAtTopCardsChooseNToHandRestToGraveyardEffect.class);

        LookAtTopCardsChooseNToHandRestToGraveyardEffect effect =
                (LookAtTopCardsChooseNToHandRestToGraveyardEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.count()).isEqualTo(4);
        assertThat(effect.toHandCount()).isEqualTo(1);
        assertThat(effect.handChoicePredicate()).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) effect.handChoicePredicate()).cardType()).isEqualTo(CardType.CREATURE);
        assertThat(effect.reveal()).isTrue();

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{2}{U}");
    }

    @Test
    @DisplayName("Resolving enters library reveal choice when multiple creatures are revealed")
    void resolvingEntersRevealChoiceState() {
        setupTopCards(List.of(new GrizzlyBears(), new GrizzlyBears(), new Shock(), new Forest()));

        harness.setHand(player1, List.of(new TrackersInstincts()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    @DisplayName("Choosing a creature puts it in hand and the rest into graveyard")
    void choosingCreaturePutsOneInHandRestInGraveyard() {
        Card bears0 = new GrizzlyBears();
        Card bears1 = new GrizzlyBears();
        Card shock = new Shock();
        Card forest = new Forest();
        setupTopCards(List.of(bears0, bears1, shock, forest));

        harness.setHand(player1, List.of(new TrackersInstincts()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(bears1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears0, shock, forest);
    }

    @Test
    @DisplayName("When only one creature is revealed, it goes to hand automatically")
    void singleCreatureAutoToHand() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card forest = new Forest();
        setupTopCards(List.of(shock, forest, bears, new Shock()));

        harness.setHand(player1, List.of(new TrackersInstincts()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock, forest);
    }

    @Test
    @DisplayName("When no creatures are revealed, all cards go to graveyard")
    void noCreaturesAllToGraveyard() {
        Card shock0 = new Shock();
        Card shock1 = new Shock();
        Card forest = new Forest();
        setupTopCards(List.of(shock0, shock1, forest, new Shock()));

        harness.setHand(player1, List.of(new TrackersInstincts()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock0, shock1, forest);
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupTopCards(List.of(new GrizzlyBears(), new Shock(), new Forest(), new Shock()));

        harness.setHand(player1, List.of(new TrackersInstincts()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tracker's Instincts"));
    }

    @Test
    @DisplayName("Flashback from graveyard works correctly")
    void flashbackFromGraveyard() {
        Card bears = new GrizzlyBears();
        setupTopCards(List.of(bears, new Shock(), new Forest(), new Shock()));

        harness.setGraveyard(player1, List.of(new TrackersInstincts()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getName().equals("Tracker's Instincts"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tracker's Instincts"));
    }

    @Test
    @DisplayName("Game log records reveal")
    void gameLogRecordsReveal() {
        setupTopCards(List.of(new GrizzlyBears(), new Shock(), new Forest(), new Shock()));

        harness.setHand(player1, List.of(new TrackersInstincts()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("reveals") && log.contains("Tracker's Instincts"));
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
