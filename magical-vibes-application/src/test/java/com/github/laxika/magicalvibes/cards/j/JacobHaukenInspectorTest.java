package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JacobHaukenInspector.class, HaukensInsight.class, GrizzlyBears.class, Island.class})
class JacobHaukenInspectorTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability draws then exiles a card from hand face down")
    void drawsThenExilesCardFromHandFaceDown() {
        Permanent inspector = addInspectorReady();
        CardPair cards = setUpLibraryAndHand();

        activateInspector(inspector);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(cards.exiled().getId());
        assertThat(entry).isNotNull();
        assertThat(entry.sourcePermanentId()).isEqualTo(inspector.getId());
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cards.drawn());
        assertThat(inspector.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Activated ability transforms after the controller pays")
    void transformsAfterPaying() {
        Permanent inspector = addInspectorReady();
        setUpLibraryAndHand();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        activateInspector(inspector);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(inspector.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Hauken's Insight exiles the top card and allows one free cast each turn")
    void backFaceExilesTopCardAndAllowsOneFreeCastEachTurn() {
        Permanent insight = addTransformedInsight();
        Card topCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        gd.addToExile(player1.getId(), secondCard, insight.getId(), true);

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.sourcePermanentId()).isEqualTo(insight.getId());
        assertThat(entry.faceDown()).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
        assertThatThrownBy(() -> harness.castFromExile(player1, secondCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addInspectorReady() {
        JacobHaukenInspector card = new JacobHaukenInspector();
        Permanent inspector = harness.addToBattlefieldAndReturn(player1, card);
        inspector.setSummoningSick(false);
        return inspector;
    }

    private Permanent addTransformedInsight() {
        JacobHaukenInspector card = new JacobHaukenInspector();
        Permanent insight = harness.addToBattlefieldAndReturn(player1, card);
        insight.setCard(card.getBackFaceCard());
        insight.setTransformed(true);
        insight.setSummoningSick(false);
        return insight;
    }

    private CardPair setUpLibraryAndHand() {
        Card drawn = new Island();
        Card exiled = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(exiled));
        return new CardPair(drawn, exiled);
    }

    private void activateInspector(Permanent inspector) {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(inspector), 0, null, null);
        harness.passBothPriorities();
    }

    private record CardPair(Card drawn, Card exiled) {
    }
}
