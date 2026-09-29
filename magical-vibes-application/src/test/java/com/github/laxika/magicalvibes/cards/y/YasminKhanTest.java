package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YasminKhan.class, Forest.class})
class YasminKhanTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card and lets its controller play it until their next end step")
    void exilesTopCardAndGrantsPlayPermissionUntilNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent yasmin = addCreatureReady(player1, new YasminKhan());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(yasmin), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(topCard.getId()));

        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }
}
