package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GolemsHeart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CemeteryProwler.class, GolemsHeart.class, GrizzlyBears.class, HillGiant.class, Juggernaut.class})
class CemeteryProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a card on entering and attacking, tracking both cards with itself")
    void exilesCardsOnEnterAndAttack() {
        Card first = new GrizzlyBears();
        Permanent prowler = enterProwlerWith(first);

        Card second = new GolemsHeart();
        exileOnAttack(prowler, second);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        assertThat(gd.getCardsExiledByPermanent(prowler.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Counts a shared card type only once when multiple exiled cards have that type")
    void countsDistinctSharedCardTypes() {
        Permanent prowler = enterProwlerWith(new GrizzlyBears());
        exileOnAttack(prowler, new GrizzlyBears());
        prepareMainPhase();

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Reduces a spell by each distinct card type shared with the exiled cards")
    void reducesForEachSharedCardType() {
        Permanent prowler = enterProwlerWith(new GrizzlyBears());
        exileOnAttack(prowler, new GolemsHeart());
        prepareMainPhase();

        harness.setHand(player1, List.of(new Juggernaut()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent enterProwlerWith(Card card) {
        harness.setGraveyard(player2, List.of(card));
        Permanent prowler = harness.enterBattlefieldAndReturn(player1, new CemeteryProwler());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(card.getId());

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        return prowler;
    }

    private void exileOnAttack(Permanent prowler, Card card) {
        prowler.setSummoningSick(false);
        harness.setGraveyard(player2, List.of(card));
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
