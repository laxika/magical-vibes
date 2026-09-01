package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HugsGrislyGuardian.class, Shock.class, Forest.class})
class HugsGrislyGuardianTest extends BaseCardTest {

    @Test
    void entersAndExilesTopXCardsUntilEndOfNextTurn() {
        Card first = new Shock();
        Card second = new Forest();
        Card third = new Shock();
        harness.setLibrary(player1, List.of(first, second, third));
        castHugs(2);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).containsEntry(first.getId(), gd.turnNumber + 2)
                .containsEntry(second.getId(), gd.turnNumber + 2);
    }

    @Test
    void exiledCardsCanBePlayedForTheirNormalCosts() {
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        castHugs(1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void controllerMayPlayAnAdditionalLandEachTurn() {
        harness.addToBattlefield(player1, new HugsGrislyGuardian());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard() instanceof Forest)
                .hasSize(2);
    }

    private void castHugs(int xValue) {
        harness.setHand(player1, List.of(new HugsGrislyGuardian()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);

        gs.playCard(gd, player1, 0, xValue, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
