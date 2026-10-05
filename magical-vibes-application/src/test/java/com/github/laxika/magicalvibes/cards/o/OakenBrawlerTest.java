package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowDodger;
import com.github.laxika.magicalvibes.cards.l.LeafGilder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OakenBrawler.class, LeafGilder.class, GoldmeadowDodger.class})
class OakenBrawlerTest extends BaseCardTest {

    private Permanent castOakenBrawler() {
        return castOakenBrawler(false);
    }

    private Permanent castOakenBrawler(boolean putBothOnBottom) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OakenBrawler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        boolean controllerReveals = !gd.playerDecks.get(player1.getId()).isEmpty();
        boolean opponentReveals = !gd.playerDecks.get(player2.getId()).isEmpty();
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (ETB clash trigger placed)
        harness.passBothPriorities(); // resolve ETB clash effect

        if (controllerReveals) {
            completePlacement(player1, putBothOnBottom);
        }
        if (opponentReveals) {
            completePlacement(player2, putBothOnBottom);
        }

        return findPermanent(player1, "Oaken Brawler");
    }

    private void completePlacement(Player player, boolean bottom) {
        gs.handleInteractionAnswer(gd, player, new InteractionAnswer.ScryOrder(
                bottom ? List.of() : List.of(0), bottom ? List.of(0) : List.of()));
    }

    @Test
    @DisplayName("Winning the clash puts a +1/+1 counter on Oaken Brawler")
    void wonClashAddsCounter() {
        // Higher mana value on top for player1 (Leaf Gilder MV 2 > Goldmeadow Dodger MV 1) → player1 wins.
        harness.setLibrary(player1, List.of(new LeafGilder()));
        harness.setLibrary(player2, List.of(new GoldmeadowDodger()));

        Permanent brawler = castOakenBrawler();

        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(brawler.getEffectivePower()).isEqualTo(3);
        assertThat(brawler.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Losing the clash leaves Oaken Brawler without a counter")
    void lostClashAddsNoCounter() {
        // Lower mana value on top for player1 (Goldmeadow Dodger MV 1 < Leaf Gilder MV 2) → player1 loses.
        harness.setLibrary(player1, List.of(new GoldmeadowDodger()));
        harness.setLibrary(player2, List.of(new LeafGilder()));

        Permanent brawler = castOakenBrawler();

        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(brawler.getEffectivePower()).isEqualTo(2);
        assertThat(brawler.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("An equal mana value tie is not a win, so no counter is added")
    void tiedClashAddsNoCounter() {
        // Equal mana values (both Leaf Gilders MV 2) → no one wins the clash.
        harness.setLibrary(player1, List.of(new LeafGilder()));
        harness.setLibrary(player2, List.of(new LeafGilder()));

        Permanent brawler = castOakenBrawler();

        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Bottom placement does not change the winner determined by the revealed cards")
    void bottomPlacementPreservesClashWin() {
        LeafGilder winningCard = new LeafGilder();
        GoldmeadowDodger opposingCard = new GoldmeadowDodger();
        OakenBrawler nextOwnCard = new OakenBrawler();
        OakenBrawler nextOpposingCard = new OakenBrawler();
        harness.setLibrary(player1, List.of(winningCard, nextOwnCard));
        harness.setLibrary(player2, List.of(opposingCard, nextOpposingCard));

        Permanent brawler = castOakenBrawler(true);

        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextOwnCard, winningCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextOpposingCard, opposingCard);
    }

    @Test
    @DisplayName("No revealed card means the controller cannot win the clash")
    void emptyControllerLibraryAddsNoCounter() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new GoldmeadowDodger()));

        Permanent brawler = castOakenBrawler();

        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Neither player wins when both libraries are empty")
    void bothLibrariesEmptyAddsNoCounter() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        Permanent brawler = castOakenBrawler();

        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
