package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrotagNightRunner.class})
class GrotagNightRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the top card with end-of-turn play permission")
    void combatDamageExilesTopCardWithPlayPermission() {
        Card topCard = topCard();
        harness.setLibrary(player1, List.of(topCard));
        Permanent runner = addCreatureReady(player1, new GrotagNightRunner());
        runner.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(topCard.getId());
    }

    @Test
    @DisplayName("Play permission expires at end of turn")
    void playPermissionExpiresAtEndOfTurn() {
        Card topCard = topCard();
        harness.setLibrary(player1, List.of(topCard));
        Permanent runner = addCreatureReady(player1, new GrotagNightRunner());
        runner.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.exilePlayPermissions).containsKey(topCard.getId());

        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
    }

    private Card topCard() {
        Card card = new Card();
        card.setType(CardType.INSTANT);
        return card;
    }
}
