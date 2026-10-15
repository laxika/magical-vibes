package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GreensideWatcher;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VizkopaConfessor.class, GrizzlyBears.class, HillGiant.class, GreensideWatcher.class,
        PlatinumEmperion.class})
class VizkopaConfessorTest extends BaseCardTest {

    private PendingInteraction.RevealCardsDiscardChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
    }

    private void castConfessor(UUID targetPlayerId) {
        harness.setHand(player1, new ArrayList<>(List.of(new VizkopaConfessor())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, 0, targetPlayerId);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger -> prompts for the life payment
    }

    @Test
    @DisplayName("Paying 2 life makes the opponent reveal two cards; the controller exiles one of them")
    void payTwoLifeRevealTwoExileOne() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant(), new GrizzlyBears())));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        castConfessor(player2.getId());

        harness.handleXValueChosen(player1, 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);

        PendingInteraction.RevealCardsDiscardChoice reveal = activeChoice();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(reveal.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1); // reveal Hill Giant as the second card

        PendingInteraction.RevealCardsDiscardChoice pick = activeChoice();
        assertThat(pick.revealStage()).isFalse();
        assertThat(pick.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(pick.revealedCardIds()).hasSize(2);

        harness.handleCardChosen(player1, 1); // exile Hill Giant

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertNotInGraveyard(player2, "Hill Giant");
        assertThat(gd.exiledCards.stream().map(e -> e.card().getName())).contains("Hill Giant");
    }

    @Test
    @DisplayName("Paying 0 life reveals nothing and exiles nothing")
    void payZeroLifeDoesNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant())));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        castConfessor(player2.getId());

        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A hand no larger than the life paid is revealed whole, skipping the opponent's reveal choice")
    void wholeHandRevealedWhenNotLarger() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        castConfessor(player2.getId());
        harness.handleXValueChosen(player1, 3);

        PendingInteraction.RevealCardsDiscardChoice pick = activeChoice();
        assertThat(pick.revealStage()).isFalse();
        assertThat(pick.decidingPlayerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(e -> e.card().getName())).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("The opponent cannot make the controller's exile pick")
    void wrongPlayerCannotMakeExilePick() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        castConfessor(player2.getId());
        harness.handleXValueChosen(player1, 1);

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    @Test
    void emptyHandStillRequiresChosenLifePayment() {
        harness.setHand(player2, List.of());
        castConfessor(player2.getId());

        harness.handleXValueChosen(player1, 3);

        harness.assertLife(player1, 17);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void cannotPayMoreLifeThanAvailableOrANegativeAmount() {
        harness.setLife(player1, 5);
        harness.setHand(player2, List.of(new GreensideWatcher()));
        castConfessor(player2.getId());

        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 6))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> harness.handleXValueChosen(player1, -1))
                .isInstanceOf(IllegalArgumentException.class);
        harness.assertLife(player1, 5);

        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 4);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void unchangedLifeTotalAllowsOnlyZeroPayment() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.setHand(player2, List.of(new GreensideWatcher()));
        castConfessor(player2.getId());

        var payment = gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        if (payment != null) {
            assertThatThrownBy(() -> harness.handleXValueChosen(player1, 1))
                    .isInstanceOf(IllegalArgumentException.class);
            harness.handleXValueChosen(player1, 0);
        }

        harness.assertLife(player1, 20);
        harness.assertInHand(player2, "Greenside Watcher");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void extortCanBePaidWithWhite() {
        harness.addToBattlefield(player1, new VizkopaConfessor());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromHand(player1, new GreensideWatcher(), "{1}{G}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void extortCanBePaidWithBlack() {
        harness.addToBattlefield(player1, new VizkopaConfessor());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromHand(player1, new GreensideWatcher(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void decliningExtortDoesNotSpendManaOrChangeLife() {
        harness.addToBattlefield(player1, new VizkopaConfessor());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromHand(player1, new GreensideWatcher(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void opponentSpellDoesNotTriggerExtort() {
        harness.addToBattlefield(player2, new VizkopaConfessor());
        harness.castFromHand(player1, new GreensideWatcher(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void extortGainsNoLifeWhenOpponentCannotLoseLife() {
        harness.addToBattlefield(player1, new VizkopaConfessor());
        harness.addToBattlefield(player2, new PlatinumEmperion());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromHand(player1, new GreensideWatcher(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }
}
