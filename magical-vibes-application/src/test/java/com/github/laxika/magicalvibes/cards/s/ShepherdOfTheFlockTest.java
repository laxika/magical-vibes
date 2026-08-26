package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UsherToSafety;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShepherdOfTheFlock.class, UsherToSafety.class, GrizzlyBears.class})
class ShepherdOfTheFlockTest extends BaseCardTest {

    @Test
    void adventureReturnsControlledPermanentAndExilesTheCardWithCreatureCastPermission() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ShepherdOfTheFlock card = new ShepherdOfTheFlock();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAdventure(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).contains(bears.getCard());
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
        assertThat(harness.getGameData().exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetPermanentControlledByOpponent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ShepherdOfTheFlock card = new ShepherdOfTheFlock();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
