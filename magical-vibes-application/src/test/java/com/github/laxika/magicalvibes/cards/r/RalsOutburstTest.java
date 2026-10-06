package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CharityExtractor;
import com.github.laxika.magicalvibes.cards.j.JayaVeneratedFiremage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RalsOutburst.class, Forest.class, CharityExtractor.class, JayaVeneratedFiremage.class})
class RalsOutburstTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a player, then puts one of the top two cards into hand and the other into the graveyard")
    void dealsDamageAndSeparatesTopCards() {
        Card chosen = new Forest();
        Card other = new CharityExtractor();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(chosen, other, remaining));
        harness.setHand(player1, List.of(new RalsOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    void mustChooseOneCardForHand() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        prepareOutburst();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
    }

    @Test
    void putsOnlyLibraryCardIntoHand() {
        Card only = new Forest();
        harness.setLibrary(player1, List.of(only));
        prepareOutburst();
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, lifeBefore - 3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(only);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryDoesNotPreventDamage() {
        harness.setLibrary(player1, List.of());
        prepareOutburst();
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, lifeBefore - 3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Ral's Outburst");
    }

    @Test
    void damagesCreatureAndStillChoosesCards() {
        var target = harness.addToBattlefieldAndReturn(player2, new CharityExtractor());
        Card chosen = new Forest();
        Card other = new Forest();
        harness.setLibrary(player1, List.of(chosen, other));
        prepareOutburst();

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
    }

    @Test
    void damagesPlaneswalkerAndStillChoosesCards() {
        var target = harness.addToBattlefieldAndReturn(player2, new JayaVeneratedFiremage());
        target.setCounterCount(CounterType.LOYALTY, 5);
        Card chosen = new Forest();
        Card other = new Forest();
        harness.setLibrary(player1, List.of(chosen, other));
        prepareOutburst();

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
    }

    @Test
    void illegalTargetPreventsLibraryChoice() {
        var target = harness.addToBattlefieldAndReturn(player2, new CharityExtractor());
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        prepareOutburst();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Ral's Outburst");
    }

    private void prepareOutburst() {
        harness.setHand(player1, List.of(new RalsOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
