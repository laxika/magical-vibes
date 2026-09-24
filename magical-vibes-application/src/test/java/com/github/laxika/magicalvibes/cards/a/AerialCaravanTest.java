package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.d.DrakeHatchling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AerialCaravan.class, DrakeHatchling.class, Island.class})
class AerialCaravanTest extends BaseCardTest {

    @Test
    @DisplayName("May play the exiled top card by paying its normal cost")
    void mayPlayExiledTopCardByPayingNormalCost() {
        addReadyCaravan();
        Card topCard = new DrakeHatchling();
        harness.setLibrary(player1, List.of(topCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drake Hatchling");
    }

    @Test
    @DisplayName("Requires the exiled spell's normal mana cost")
    void requiresNormalManaToPlayExiledSpell() {
        addReadyCaravan();
        Card topCard = new DrakeHatchling();
        harness.setLibrary(player1, List.of(topCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("May play an exiled land")
    void mayPlayExiledLand() {
        addReadyCaravan();
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void emptyLibraryExilesNothing() {
        addReadyCaravan();
        harness.setLibrary(player1, List.of());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Play permission expires at end of turn")
    void playPermissionExpiresAtEndOfTurn() {
        addReadyCaravan();
        Card topCard = new DrakeHatchling();
        harness.setLibrary(player1, List.of(topCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    private void addReadyCaravan() {
        addCreatureReady(player1, new AerialCaravan());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }
}
