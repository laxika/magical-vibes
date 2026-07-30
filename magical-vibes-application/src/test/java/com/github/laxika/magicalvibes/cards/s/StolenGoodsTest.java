package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StolenGoodsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles through lands until a nonland card and grants a free cast this turn")
    void exilesUntilNonlandAndGrantsFreeCast() {
        Forest land1 = new Forest();
        Forest land2 = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, new ArrayList<>(List.of(land1, land2, bears)));

        harness.setHand(player1, new ArrayList<>(List.of(new StolenGoods())));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Forest", "Grizzly Bears");
        assertThat(gd.exilePlayPermissions.get(bears.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(bears.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(bears.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land1.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land2.getId());
    }

    @Test
    @DisplayName("Free-cast permission expires at end of turn")
    void permissionExpiresAtEndOfTurn() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, new ArrayList<>(List.of(bears)));

        harness.setHand(player1, new ArrayList<>(List.of(new StolenGoods())));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(bears.getId());

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(bears.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(bears.getId());
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new StolenGoods())));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
