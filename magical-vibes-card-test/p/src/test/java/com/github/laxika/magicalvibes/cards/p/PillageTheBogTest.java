package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PillageTheBog.class, Forest.class, GrizzlyBears.class, Shock.class})
class PillageTheBogTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at twice the lands controlled, keeps one, and randomly bottoms the rest")
    void looksAtTwiceLandsAndBottomsUnchosenCards() {
        Card[] top = {
                new GrizzlyBears(), new Shock(), new GrizzlyBears(),
                new Shock(), new GrizzlyBears(), new Shock()
        };
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new PillageTheBog()));
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(top[1].getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top[1]);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).startsWith(top[4], top[5]);
        assertThat(deck.subList(2, deck.size())).containsExactlyInAnyOrder(top[0], top[2], top[3]);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void zeroLandsLeavesLibraryUnchanged() {
        Card top = new Forest();
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new PillageTheBog()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void oneCardLibraryPutsOnlyAvailableCardIntoHand() {
        Card top = new Forest();
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new PillageTheBog()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void landCountIsDeterminedAtResolutionAndChoosingIsMandatory() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new PillageTheBog()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new Forest());

        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void plottingPaysFullCostAndAllowsFreeCastOnlyOnLaterTurn() {
        PillageTheBog spell = new PillageTheBog();
        Card draw = new Forest();
        Card first = new Forest();
        Card second = new Forest();
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(draw, first, second));
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw, first, second);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(draw, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }
}
