package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmberheartChallenger.class, Forest.class, GiantGrowth.class})
class EmberheartChallengerTest extends BaseCardTest {

    @Test
    @DisplayName("Valiant exiles the top card with permission to play it this turn")
    void valiantExilesTopCardWithPlayPermission() {
        Permanent challenger = addChallenger();
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        castGrowth(player1, challenger);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("Valiant triggers only once each turn")
    void valiantTriggersOnlyOnceEachTurn() {
        Permanent challenger = addChallenger();
        Forest firstTopCard = new Forest();
        Forest secondTopCard = new Forest();
        harness.setLibrary(player1, List.of(firstTopCard, secondTopCard));

        castGrowth(player1, challenger);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, challenger.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(firstTopCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondTopCard);
    }

    @Test
    @DisplayName("Valiant does not trigger for an opponent's spell")
    void valiantDoesNotTriggerForOpponentsSpell() {
        Permanent challenger = addChallenger();
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, challenger.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    private Permanent addChallenger() {
        return harness.addToBattlefieldAndReturn(player1, new EmberheartChallenger());
    }

    private void castGrowth(com.github.laxika.magicalvibes.model.Player player, Permanent target) {
        harness.setHand(player, List.of(new GiantGrowth()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.castInstant(player, 0, target.getId());
        harness.passBothPriorities();
    }
}
