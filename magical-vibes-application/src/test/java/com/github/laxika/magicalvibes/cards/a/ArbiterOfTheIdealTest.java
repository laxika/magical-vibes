package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SisaysRing;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArbiterOfTheIdealTest extends BaseCardTest {

    @Test
    @DisplayName("Inspired puts a matching top card onto the battlefield with its counter and enchantment type")
    void putsMatchingCardOntoBattlefieldWithModifications() {
        Permanent arbiter = addTappedArbiter(player1);
        Card ring = new SisaysRing();
        harness.setLibrary(player1, deckOf(ring));

        resolveUntapTrigger(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(ring.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(entered.getCounterCount(CounterType.MANIFESTATION)).isEqualTo(1);
        assertThat(gqs.isEnchantment(gd, entered)).isTrue();
        assertThat(arbiter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Solemnity prevents the manifestation counter but not the enchantment type")
    void solemnityPreventsManifestationCounter() {
        addTappedArbiter(player1);
        harness.addToBattlefield(player1, new Solemnity());
        Card ring = new SisaysRing();
        harness.setLibrary(player1, deckOf(ring));

        resolveUntapTrigger(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(ring.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(entered.getCounterCount(CounterType.MANIFESTATION)).isZero();
        assertThat(gqs.isEnchantment(gd, entered)).isTrue();
    }

    @Test
    @DisplayName("Inspired leaves a nonmatching top card available to draw without offering a choice")
    void nonmatchingCardStaysOnTop() {
        addTappedArbiter(player1);
        Card nonmatching = new Card();
        nonmatching.setName("Nonmatching Spell");
        nonmatching.setType(com.github.laxika.magicalvibes.model.CardType.INSTANT);
        harness.setLibrary(player1, deckOf(nonmatching));

        resolveUntapTrigger(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(nonmatching.getId()));
        harness.assertNotOnBattlefield(player1, "Nonmatching Spell");
    }

    @Test
    @DisplayName("Declining Inspired leaves the matching top card available to draw")
    void declineLeavesMatchingCardOnTop() {
        addTappedArbiter(player1);
        Card land = new Forest();
        harness.setLibrary(player1, deckOf(land));

        resolveUntapTrigger(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(land.getId()));
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    private Permanent addTappedArbiter(Player player) {
        Permanent arbiter = harness.addToBattlefieldAndReturn(player, new ArbiterOfTheIdeal());
        arbiter.setSummoningSick(false);
        arbiter.tap();
        return arbiter;
    }

    private void resolveUntapTrigger(Player activePlayer) {
        Player opponent = activePlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private List<Card> deckOf(Card... cards) {
        return new ArrayList<>(List.of(cards));
    }
}
