package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IxhelScionOfAtraxa.class, GrizzlyBears.class})
class IxhelScionOfAtraxaTest extends BaseCardTest {

    @Test
    @DisplayName("Does not exile an opponent's card while they have fewer than three poison counters")
    void requiresThreePoisonCounters() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.addToBattlefield(player1, new IxhelScionOfAtraxa());

        resolveEndStepTrigger();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Exiles an eligible opponent's top card with persistent any-color play permission")
    void exilesTopCardForCorruptedOpponent() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addToBattlefield(player1, new IxhelScionOfAtraxa());

        resolveEndStepTrigger();

        ExiledCardEntry exiled = gd.findExiledCard(topCard.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.ownerId()).isEqualTo(player2.getId());
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(topCard.getId());
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();
    }
}
