package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HithlainRope;
import com.github.laxika.magicalvibes.cards.t.TajuruPreserver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PoxPlague.class, Forest.class, GrizzlyBears.class, Peek.class})
class PoxPlagueTest extends BaseCardTest {

    @Test
    void losesAndDiscardsHalfRoundedDownPerPlayer() {
        harness.setLife(player1, 19);
        harness.setLife(player2, 9);
        harness.setHand(player1, new ArrayList<>(List.of(
                new PoxPlague(), new GrizzlyBears(), new Peek(), new Forest(), new Forest())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(5);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void sacrificesHalfOfAllPermanentsRoundedDown() {
        harness.setHand(player1, List.of(new PoxPlague()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).hasSize(4);
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, choice.validIds().subList(0, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void singleCardPermanentAndLifeRoundDownToZeroLoss() {
        harness.setLife(player1, 1);
        harness.setLife(player2, 1);
        harness.setHand(player1, List.of(new PoxPlague(), new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 1);
        harness.assertLife(player2, 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Pox Plague");
    }

    @Test
    void oddPermanentCountsAreRoundedDownAndSacrificedOnlyAfterBothPlayersChoose() {
        harness.setHand(player1, List.of(new PoxPlague(), new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new Forest());
        }
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        PendingInteraction.MultiPermanentChoice first =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(first.playerId()).isEqualTo(player1.getId());
        assertThat(first.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player1, first.validIds().subList(0, 1));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(5);
        PendingInteraction.MultiPermanentChoice second =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(second.playerId()).isEqualTo(player2.getId());
        assertThat(second.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player2, second.validIds().subList(0, 2));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Pox Plague");
    }

    @Test
    void discardSelectionsRemainHiddenUntilBothPlayersHaveChosen() {
        harness.setHand(player1, List.of(new PoxPlague(), new GrizzlyBears(), new Forest()));
        harness.setHand(player2, List.of(new Peek(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Peek");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @CardUsed({TajuruPreserver.class})
    void opponentsSacrificeProtectionDoesNotPreventLifeLossOrDiscard() {
        harness.setHand(player1, List.of(new PoxPlague()));
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player2, new TajuruPreserver());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 10);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        harness.assertOnBattlefield(player2, "Tajuru Preserver");
    }

    @Test
    @CardUsed({HithlainRope.class})
    void unsacrificablePermanentCountsTowardHalfButCannotBeChosen() {
        harness.setHand(player1, List.of(new PoxPlague()));
        harness.setHand(player2, List.of());
        var rope = harness.addToBattlefieldAndReturn(player1, new HithlainRope());
        var firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        var secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());
        assertThat(choice.validIds()).doesNotContain(rope.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(firstLand.getId()));

        harness.assertOnBattlefield(player1, "Hithlain Rope");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

}
