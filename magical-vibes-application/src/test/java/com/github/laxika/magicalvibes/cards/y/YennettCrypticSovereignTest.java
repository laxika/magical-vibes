package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Harrow;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.v.Vandalblast;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YennettCrypticSovereign.class, LlanowarElves.class, GrizzlyBears.class, Forest.class,
        Vandalblast.class, Harrow.class})
class YennettCrypticSovereignTest extends BaseCardTest {

    @Test
    void attacksOfferOddManaValueCardForFreeCast() {
        LlanowarElves topCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(topCard));
        addReadyYennett();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void evenManaValueCardIsDrawn() {
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new Forest()));
        addReadyYennett();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void decliningOddManaValueCardDrawsIt() {
        LlanowarElves topCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(topCard, new Forest()));
        addReadyYennett();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void landIsDrawnRatherThanPlayed() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, new Forest()));
        addReadyYennett();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void emptyLibraryStillAttemptsDrawAndCausesLoss() {
        harness.setLibrary(player1, List.of());
        addReadyYennett();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void oddSpellWithoutLegalTargetsIsDrawn() {
        Vandalblast topCard = new Vandalblast();
        harness.setLibrary(player1, List.of(topCard, new Forest()));
        addReadyYennett();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void oddSpellWithUnpayableAdditionalCostIsDrawn() {
        Harrow topCard = new Harrow();
        harness.setLibrary(player1, List.of(topCard, new Forest()));
        addReadyYennett();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == topCard);
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    private void addReadyYennett() {
        addCreatureReady(player1, new YennettCrypticSovereign());
    }
}
