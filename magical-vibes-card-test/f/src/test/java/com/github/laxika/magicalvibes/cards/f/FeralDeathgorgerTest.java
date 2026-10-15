package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeralDeathgorger.class, GrizzlyBears.class, Plains.class, Shock.class})
class FeralDeathgorgerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to two cards from a single graveyard")
    void etbExilesUpToTwoCardsFromSingleGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card third = new Plains();
        harness.setGraveyard(player2, List.of(first, second, third));
        FeralDeathgorger card = new FeralDeathgorger();
        harness.castFromHand(player1, card, "{5}{B}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(third);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Omen puts a counter on a creature, draws, and shuffles the card into its owner's library")
    void omenPutsCounterDrawsAndShuffles() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Card drawn = new Plains();
        FeralDeathgorger card = new FeralDeathgorger();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    void omenWithoutTargetDrawsAndShufflesExactlyOnePhysicalCard() {
        Card drawn = new Plains();
        FeralDeathgorger card = new FeralDeathgorger();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    void etbCanExileOneCardFromOwnGraveyard() {
        Card chosen = new Plains();
        Card retained = new FeralDeathgorger();
        harness.setGraveyard(player1, List.of(chosen, retained));
        harness.castFromHand(player1, new FeralDeathgorger(), "{5}{B}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(retained);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen);
    }

    @Test
    void etbCanChooseZeroTargets() {
        Card retained = new Plains();
        harness.setGraveyard(player2, List.of(retained));
        harness.castFromHand(player1, new FeralDeathgorger(), "{5}{B}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(retained);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Feral Deathgorger");
    }

    @Test
    void etbRejectsTargetsFromDifferentGraveyards() {
        Card own = new Plains();
        Card opponents = new Plains();
        harness.setGraveyard(player1, List.of(own));
        harness.setGraveyard(player2, List.of(opponents));
        harness.castFromHand(player1, new FeralDeathgorger(), "{5}{B}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(own.getId(), opponents.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");

        harness.handleMultipleCardsChosen(player1, List.of(own.getId()));
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(own);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponents);
    }

    @Test
    void omenDoesNotDrawOrShuffleWhenChosenTargetBecomesIllegal() {
        Permanent target = addCreatureReady(player2, new FeralDeathgorger());
        Card drawn = new Plains();
        FeralDeathgorger card = new FeralDeathgorger();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
    }
}
