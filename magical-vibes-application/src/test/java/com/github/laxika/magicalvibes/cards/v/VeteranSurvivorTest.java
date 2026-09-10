package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeteranSurvivor.class, GrizzlyBears.class, HillGiant.class})
class VeteranSurvivorTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped Veteran Survivor exiles and tracks up to one card from any graveyard")
    void tappedSurvivorExilesAndTracksCard() {
        Permanent survivor = addSurvivor();
        Card ownCard = new GrizzlyBears();
        Card opposingCard = new HillGiant();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opposingCard));
        survivor.tap();

        advanceToPostcombatMain(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.minCount()).isZero();
        assertThat(choice.validCardIds()).containsExactly(ownCard.getId(), opposingCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(opposingCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(survivor.getId())).containsExactly(opposingCard);
    }

    @Test
    @DisplayName("The up-to-one Survival choice may be declined")
    void mayDeclineExile() {
        Permanent survivor = addSurvivor();
        Card card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));
        survivor.tap();

        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
        assertThat(gd.getCardsExiledByPermanent(survivor.getId())).isEmpty();
    }

    @Test
    @DisplayName("Three cards exiled with Veteran Survivor grant +3/+3 and hexproof")
    void thresholdGrantsBoostAndHexproof() {
        Permanent survivor = addSurvivor();
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        Card third = new GrizzlyBears();
        gd.addToExile(player1.getId(), first, survivor.getId());
        gd.addToExile(player1.getId(), second, survivor.getId());
        gd.addToExile(player1.getId(), third, survivor.getId());

        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, survivor, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("An untapped Veteran Survivor does not trigger Survival")
    void untappedSurvivorDoesNotTrigger() {
        addSurvivor();
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    private Permanent addSurvivor() {
        return harness.addToBattlefieldAndReturn(player1, new VeteranSurvivor());
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
