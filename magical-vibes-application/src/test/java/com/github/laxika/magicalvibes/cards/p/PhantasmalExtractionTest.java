package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PsychicIntrusion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhantasmalExtraction.class, Forest.class, GrizzlyBears.class, PsychicIntrusion.class})
class PhantasmalExtractionTest extends BaseCardTest {

    @Test
    void onlyAllowsEligibleHandCardsAndExilesTheChosenCard() {
        Card land = new Forest();
        Card eligibleCard = new GrizzlyBears();
        Card tooExpensiveCard = new PsychicIntrusion();
        harness.setHand(player1, List.of(land, eligibleCard, tooExpensiveCard));

        castAsNonStartingPlayer();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(land.getId())))
                .hasMessageContaining("valid nonland card");
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(tooExpensiveCard.getId())))
                .hasMessageContaining("valid nonland card");

        harness.handleMultipleCardsChosen(player2, List.of(eligibleCard.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(eligibleCard);
    }

    @Test
    void canExileAnyCardFromTheTargetGraveyard() {
        Card graveyardLand = new Forest();
        harness.setHand(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(graveyardLand));

        castAsNonStartingPlayer();
        harness.handleMultipleCardsChosen(player2, List.of(graveyardLand.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(graveyardLand);
    }

    @Test
    void nonStartingPlayerGetsCostReduction() {
        forceNonStartingPlayer();
        harness.setHand(player2, List.of(new PhantasmalExtraction()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castSorcery(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void startingPlayerDoesNotGetCostReduction() {
        harness.setHand(player1, List.of(new PhantasmalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAsNonStartingPlayer() {
        forceNonStartingPlayer();
        harness.setHand(player2, List.of(new PhantasmalExtraction()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
    }

    private void forceNonStartingPlayer() {
        harness.forceActivePlayer(player2);
        gd.startingPlayerId = player1.getId();
    }
}
