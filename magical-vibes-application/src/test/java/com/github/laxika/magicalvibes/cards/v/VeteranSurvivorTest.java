package com.github.laxika.magicalvibes.cards.v;

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

@CardUsed(VeteranSurvivor.class)
class VeteranSurvivorTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped Veteran Survivor exiles and tracks up to one card from any graveyard")
    void tappedSurvivorExilesAndTracksCard() {
        Permanent survivor = addSurvivor();
        Card ownCard = new VeteranSurvivor();
        Card opposingCard = new VeteranSurvivor();
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
        Card card = new VeteranSurvivor();
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
        Card first = new VeteranSurvivor();
        Card second = new VeteranSurvivor();
        Card third = new VeteranSurvivor();
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
        harness.setGraveyard(player2, List.of(new VeteranSurvivor()));

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Survival rechecks whether Veteran Survivor is tapped on resolution")
    void untappingBeforeResolutionPreventsExile() {
        Permanent survivor = addSurvivor();
        Card card = new VeteranSurvivor();
        harness.setGraveyard(player2, List.of(card));
        survivor.tap();

        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        survivor.untap();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
        assertThat(gd.getCardsExiledByPermanent(survivor.getId())).isEmpty();
    }

    @Test
    @DisplayName("Survival does not trigger during the opponent's second main phase")
    void doesNotTriggerDuringOpponentsTurn() {
        Permanent survivor = addSurvivor();
        survivor.tap();
        harness.setGraveyard(player2, List.of(new VeteranSurvivor()));

        advanceToPostcombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Survival can resolve without a target when both graveyards are empty")
    void emptyGraveyardsAllowZeroTargets() {
        Permanent survivor = addSurvivor();
        survivor.tap();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        advanceToPostcombatMain(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(survivor.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiling the third card turns on the bonus only for its source")
    void thirdExileEnablesBonusOnlyForItsSource() {
        Permanent survivor = addSurvivor();
        Permanent other = addSurvivor();
        gd.addToExile(player1.getId(), new VeteranSurvivor(), survivor.getId());
        gd.addToExile(player1.getId(), new VeteranSurvivor(), survivor.getId());
        Card third = new VeteranSurvivor();
        harness.setGraveyard(player2, List.of(third));
        survivor.tap();

        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, survivor, Keyword.HEXPROOF)).isFalse();

        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(third.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, survivor, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, other, Keyword.HEXPROOF)).isFalse();

        gd.removeFromExile(third.getId());
        gd.playerGraveyards.get(player2.getId()).add(third);

        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, survivor, Keyword.HEXPROOF)).isFalse();
    }

    private Permanent addSurvivor() {
        return harness.addToBattlefieldAndReturn(player1, new VeteranSurvivor());
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
