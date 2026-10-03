package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CatharticPyre.class, ChandraNalaar.class, Forest.class, GrizzlyBears.class, Island.class,
        Mountain.class})
class CatharticPyreTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a target creature")
    void damagesTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CatharticPyre()));
        addMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 3 damage to a target planeswalker")
    void damagesTargetPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new CatharticPyre()));
        addMana();

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Discards up to two cards, then draws that many")
    void discardsTwoThenDrawsTwo() {
        harness.setHand(player1, List.of(new CatharticPyre(), new GrizzlyBears(), new Island()));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        addMana();

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Cathartic Pyre", "Grizzly Bears", "Island");
    }

    @Test
    @DisplayName("Choosing zero discards and draws no cards")
    void choosesZeroCards() {
        harness.setHand(player1, List.of(new CatharticPyre(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        addMana();

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Cannot target a player with the damage mode")
    void damageModeCannotTargetPlayer() {
        harness.setHand(player1, List.of(new CatharticPyre()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can discard one card while keeping another, then draw exactly one")
    void discardsOneThenDrawsOne() {
        harness.setHand(player1, List.of(new CatharticPyre(), new Island(), new Mountain()));
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        addMana();

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Mountain", "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Island");
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("The discard mode resolves with an empty hand without drawing")
    void emptyHandDrawsNothing() {
        harness.setHand(player1, List.of(new CatharticPyre()));
        harness.setLibrary(player1, List.of(new Forest()));
        addMana();

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Cathartic Pyre");
    }

    @Test
    @DisplayName("Cannot discard more than two even with a larger hand")
    void cannotDiscardMoreThanTwo() {
        harness.setHand(player1, List.of(new CatharticPyre(), new Island(), new Mountain(), new Forest()));
        addMana();

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 3))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleXValueChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot target a land with the damage mode")
    void damageModeCannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new CatharticPyre()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0,
                harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target the controller's own creature")
    void damagesOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CatharticPyre()));
        addMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
