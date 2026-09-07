package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.z.ZurgoHelmsmasher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DjeruAndHazoret.class, Forest.class, GrizzlyBears.class, Mountain.class, ZurgoHelmsmasher.class})
class DjeruAndHazoretTest extends BaseCardTest {

    @Test
    void gainsHasteAndVigilanceWithOneOrFewerCardsInHand() {
        Permanent djeruAndHazoret = harness.addToBattlefieldAndReturn(player1, new DjeruAndHazoret());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));

        assertThat(djeruAndHazoret.isAttacking()).isTrue();
        assertThat(djeruAndHazoret.isTapped()).isFalse();
    }

    @Test
    void doesNotGainHasteWithMoreThanOneCardInHand() {
        harness.addToBattlefield(player1, new DjeruAndHazoret());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attacksToOfferOnlyLegendaryCreatureAndCastItForFree() {
        addReadyDjeruAndHazoret();
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card zurgo = new ZurgoHelmsmasher();
        setLibrary(List.of(forest, bears, zurgo, new Mountain(), new Forest(), new GrizzlyBears(), new Mountain()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(zurgo);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(zurgo);
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(zurgo.getId());

        harness.castFromExile(player1, zurgo.getId());
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == zurgo
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() == zurgo);
    }

    @Test
    void mayDeclineAndPutAllLookedCardsOnBottom() {
        addReadyDjeruAndHazoret();
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card zurgo = new ZurgoHelmsmasher();
        setLibrary(List.of(forest, bears, zurgo, new Mountain(), new Forest(), new GrizzlyBears(), new Mountain()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
    }

    private Permanent addReadyDjeruAndHazoret() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        return addCreatureReady(player1, new DjeruAndHazoret());
    }

    private void setLibrary(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}
