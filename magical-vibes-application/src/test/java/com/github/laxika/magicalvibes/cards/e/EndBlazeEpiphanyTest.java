package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EndBlazeEpiphanyTest extends BaseCardTest {

    private void resolveUntilChoiceOrDone() {
        int guard = 0;
        while (gd.interaction.activeInteraction() == null && !gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Deals X damage and lets you play one card among the cards exiled for the dying creature's power")
    void exilesCardsEqualToDyingPowerAndGrantsChosenCardPermission() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card first = new Shock();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new EndBlazeEpiphany()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 2, harness.getPermanentId(player2, "Grizzly Bears"));
        resolveUntilChoiceOrDone();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.exilePlayPermissions).containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(first.getId());
    }

    @Test
    @DisplayName("Does not exile cards when the targeted creature survives")
    void survivingCreatureDoesNotTriggerExile() {
        harness.addToBattlefield(player2, new AirElemental());
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new EndBlazeEpiphany()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 2, harness.getPermanentId(player2, "Air Elemental"));
        resolveUntilChoiceOrDone();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Rejects a non-creature target")
    void rejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new EndBlazeEpiphany()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
