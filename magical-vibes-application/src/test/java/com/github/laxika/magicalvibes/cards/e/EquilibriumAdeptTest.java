package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EquilibriumAdept.class, DarkRitual.class, Island.class})
class EquilibriumAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles the top card with permission to play it until the end of your next turn")
    void etbExilesTopCardWithNextTurnPlayPermission() {
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new EquilibriumAdept()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd.get(topCard.getId()))
                .isEqualTo(gd.turnNumber + 2);
    }

    @Test
    @DisplayName("Flurry grants double strike on the second spell each turn")
    void flurryGrantsDoubleStrikeOnSecondSpell() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new EquilibriumAdept());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, adept, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, adept, Keyword.DOUBLE_STRIKE)).isTrue();
    }
}
