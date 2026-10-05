package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Crypsis;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PainSeer.class, Forest.class, GrizzlyBears.class, Crypsis.class, TurnToFrog.class})
class PainSeerTest extends BaseCardTest {

    @Test
    @DisplayName("When Pain Seer becomes untapped, it puts the top card into hand and loses its mana value in life")
    void becomesUntappedRevealsTopCardAndLosesLife() {
        addTappedPainSeer(player1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, deckOf(topCard));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        resolveUntapTrigger(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Putting a land into hand after becoming untapped causes no life loss")
    void landCausesNoLifeLoss() {
        addTappedPainSeer(player1);
        Card topCard = new Forest();
        harness.setLibrary(player1, deckOf(topCard));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        resolveUntapTrigger(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Becoming untapped does nothing when the library is empty")
    void emptyLibraryDoesNothing() {
        addTappedPainSeer(player1);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        resolveUntapTrigger(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An already untapped Pain Seer does not trigger during untap")
    void alreadyUntappedDoesNotTrigger() {
        harness.addToBattlefield(player1, new PainSeer());
        Card topCard = new PainSeer();
        harness.setLibrary(player1, deckOf(topCard));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        resolveUntapTrigger(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The opponent's Pain Seer uses its controller's library and life total")
    void opponentSeerUsesItsControllersLibraryAndLife() {
        addTappedPainSeer(player2);
        Card topCard = new PainSeer();
        harness.setLibrary(player2, deckOf(topCard));
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveUntapTrigger(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The inspired trigger still resolves after Pain Seer leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent seer = addTappedPainSeer(player1);
        Card topCard = new PainSeer();
        harness.setLibrary(player1, deckOf(topCard));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        resolveUntapTrigger(player1);
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, seer));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard, seer.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Untapping Pain Seer with a spell triggers inspired outside the untap step")
    void spellUntapTriggersInspired() {
        Permanent seer = addTappedPainSeer(player1);
        Card topCard = new PainSeer();
        harness.setLibrary(player1, deckOf(topCard));
        harness.setHand(player1, List.of(new Crypsis()));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, seer.getId());

        assertThat(seer.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Pain Seer does not trigger when it has lost all abilities before becoming untapped")
    void noInspiredTriggerAfterLosingAllAbilities() {
        Permanent seer = addTappedPainSeer(player1);
        Card topCard = new PainSeer();
        harness.setLibrary(player1, deckOf(topCard));
        harness.setHand(player1, List.of(new TurnToFrog(), new Crypsis()));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, seer.getId());
        harness.castAndResolveInstant(player1, 0, seer.getId());

        assertThat(seer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 20);
    }

    private Permanent addTappedPainSeer(Player player) {
        Permanent painSeer = harness.addToBattlefieldAndReturn(player, new PainSeer());
        painSeer.setSummoningSick(false);
        painSeer.tap();
        return painSeer;
    }

    private void resolveUntapTrigger(Player activePlayer) {
        Player opponent = activePlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(activePlayer, TurnStep.UPKEEP);
    }

    private List<Card> deckOf(Card... cards) {
        return new ArrayList<>(List.of(cards));
    }
}
