package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EerieUltimatum.class, Forest.class, GrizzlyBears.class, HolyDay.class})
class EerieUltimatumTest extends BaseCardTest {

    @Test
    void returnsAnyNumberOfPermanentCardsWithDifferentNamesSimultaneously() {
        Card bears = new GrizzlyBears();
        Card duplicateBears = new GrizzlyBears();
        Card forest = new Forest();
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(bears, duplicateBears, forest, instant));
        castEerieUltimatum();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class))
                .isNotNull();
        harness.handleGraveyardCardChosen(player1, 0);

        PendingInteraction.GraveyardChoice nextChoice = gd.interaction
                .activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(nextChoice).isNotNull();
        assertThat(nextChoice.cardPool()).extracting(Card::getId).containsExactly(forest.getId());
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getId())
                .containsExactlyInAnyOrder(bears.getId(), forest.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(duplicateBears.getId(), instant.getId());
    }

    @Test
    void mayReturnZeroCards() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        castEerieUltimatum();

        harness.handleGraveyardCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castEerieUltimatum() {
        harness.setHand(player1, List.of(new EerieUltimatum()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
