package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.n.NissaGenesisMage;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeployTheGatewatch.class, ChandraNalaar.class, JaceBeleren.class,
        NissaGenesisMage.class, Shock.class})
class DeployTheGatewatchTest extends BaseCardTest {

    @Test
    @DisplayName("puts up to two planeswalkers from the top seven onto the battlefield")
    void putsUpToTwoPlaneswalkersOntoBattlefield() {
        Card jace = new JaceBeleren();
        Card nissa = new NissaGenesisMage();
        Card chandra = new ChandraNalaar();
        Card shock = new Shock();
        setLibrary(jace, nissa, chandra, shock);

        castDeployTheGatewatch();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                jace.getId(), nissa.getId(), chandra.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(jace.getId(), nissa.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(jace, nissa);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(chandra, shock);
    }

    @Test
    @DisplayName("may decline and puts non-planeswalkers back on the bottom")
    void mayDeclineAndBottomsNonPlaneswalkers() {
        Card jace = new JaceBeleren();
        Card shock = new Shock();
        setLibrary(jace, shock);

        castDeployTheGatewatch();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(jace, shock);
    }

    @Test
    @DisplayName("may choose just one planeswalker even when two are available")
    void mayChooseOnlyOnePlaneswalker() {
        Card jace = new JaceBeleren();
        Card nissa = new NissaGenesisMage();
        setLibrary(jace, nissa);

        castDeployTheGatewatch();
        harness.handleMultipleCardsChosen(player1, List.of(nissa.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(nissa);
        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(permanent.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(permanent.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(jace);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("looks at only seven cards and bottoms the rest below the untouched library")
    void leavesCardsBelowTopSevenUntouched() {
        Card jace = new JaceBeleren();
        Card nissa = new NissaGenesisMage();
        List<Card> remaining = List.of(new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock());
        harness.setLibrary(player1, List.of(jace, remaining.get(0), remaining.get(1),
                remaining.get(2), remaining.get(3), remaining.get(4), remaining.get(5), nissa));

        castDeployTheGatewatch();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(jace.getId());
        harness.handleMultipleCardsChosen(player1, List.of(jace.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(jace);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(nissa);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                .containsExactlyInAnyOrderElementsOf(remaining);
    }

    @Test
    @DisplayName("bottoms all looked-at cards without a choice when none are planeswalkers")
    void noEligibleCardsAreBottomedAutomatically() {
        List<Card> lookedAt = List.of(new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock());
        Card jace = new JaceBeleren();
        harness.setLibrary(player1, List.of(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2),
                lookedAt.get(3), lookedAt.get(4), lookedAt.get(5), lookedAt.get(6), jace));

        castDeployTheGatewatch();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(jace);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 8))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
    }

    @Test
    @DisplayName("resolves without a choice when the library is empty")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());

        castDeployTheGatewatch();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void castDeployTheGatewatch() {
        harness.setHand(player1, List.of(new DeployTheGatewatch()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
