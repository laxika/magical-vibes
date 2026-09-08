package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IreShaman.class})
class IreShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Turning Ire Shaman face up adds a counter and exiles the top card with play permission")
    void turningFaceUpAddsCounterAndExilesTopCardWithPlayPermission() {
        Card topCard = topCard("Exiled Card");
        harness.setLibrary(player1, List.of(topCard));
        Permanent shaman = castFaceDown();

        turnFaceUp(shaman);
        assertThat(shaman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(topCard.getId());
    }

    @Test
    @DisplayName("Ire Shaman's play permission expires at end of turn")
    void playPermissionExpiresAtEndOfTurn() {
        Card topCard = topCard("Exiled Card");
        harness.setLibrary(player1, List.of(topCard));
        Permanent shaman = castFaceDown();

        turnFaceUp(shaman);
        harness.passBothPriorities();
        assertThat(gd.exilePlayPermissions).containsKey(topCard.getId());

        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
    }

    private Card topCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        return card;
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new IreShaman()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        return findPermanent(player1, "Ire Shaman");
    }

    private void turnFaceUp(Permanent shaman) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shaman));
    }
}
