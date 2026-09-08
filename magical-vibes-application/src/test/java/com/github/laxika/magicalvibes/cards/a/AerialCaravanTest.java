package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AerialCaravanTest extends BaseCardTest {

    @Test
    @DisplayName("May play the exiled top card by paying its normal cost")
    void mayPlayExiledTopCardByPayingNormalCost() {
        addReadyCaravan();
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("May play an exiled land")
    void mayPlayExiledLand() {
        addReadyCaravan();
        Card topCard = new Swamp();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Swamp");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Play permission expires at end of turn")
    void playPermissionExpiresAtEndOfTurn() {
        addReadyCaravan();
        Card topCard = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    private void addReadyCaravan() {
        Permanent caravan = new Permanent(new AerialCaravan());
        caravan.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(caravan);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }
}
