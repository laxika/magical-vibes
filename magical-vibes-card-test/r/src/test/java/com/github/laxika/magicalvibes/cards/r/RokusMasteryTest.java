package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RokusMastery.class, GrizzlyBears.class, HillGiant.class})
class RokusMasteryTest extends BaseCardTest {

    @Test
    void dealsXDamageToTargetCreatureWithoutScryingBelowFour() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new RokusMastery()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 2, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void dealsXDamageAndScriesTwoAtFour() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setHand(player1, List.of(new RokusMastery()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, 4, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        harness.assertInGraveyard(player2, "Hill Giant");

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void zeroXDealsNoDamageAndDoesNotScry() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setHand(player1, List.of(new RokusMastery()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Hill Giant").getMarkedDamage()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Roku's Mastery");
    }

    @Test
    void killingCreatureAtThreeDoesNotScry() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setHand(player1, List.of(new RokusMastery()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, 3, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void aboveFourScriesOnlyTwoAndAllowsTopBottomSplit() {
        var top = new GrizzlyBears();
        var second = new HillGiant();
        var third = new RokusMastery();
        var opponentTop = new HillGiant();
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(top, second, third));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.setHand(player1, List.of(new RokusMastery()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstant(player1, 0, 5, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        var scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(top, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Roku's Mastery");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canReorderBothCardsOnTop() {
        var top = new GrizzlyBears();
        var second = new HillGiant();
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLibrary(player1, List.of(top, second));
        harness.setHand(player1, List.of(new RokusMastery()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, 4, harness.getPermanentId(player1, "Hill Giant"));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    void scriesAvailableCardWhenLibraryHasOnlyOneCard() {
        var top = new GrizzlyBears();
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new RokusMastery()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, 4, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        var scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.assertInGraveyard(player1, "Roku's Mastery");
    }

    @Test
    void emptyLibraryDoesNotPreventDamageOrSpellCompletion() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new RokusMastery()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, 4, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Roku's Mastery");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotScryWhenItsOnlyTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setHand(player1, List.of(new RokusMastery(), new RokusMastery()));
        harness.addMana(player1, ManaColor.RED, 11);
        var target = harness.getPermanentId(player2, "Hill Giant");

        harness.castInstant(player1, 0, 4, target);
        harness.castInstant(player1, 0, 3, target);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Hill Giant");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof RokusMastery).hasSize(2);
    }
}
