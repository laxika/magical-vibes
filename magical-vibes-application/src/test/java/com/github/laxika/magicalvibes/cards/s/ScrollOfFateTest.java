package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScrollOfFate.class, GrizzlyBears.class})
class ScrollOfFateTest extends BaseCardTest {

    @Test
    void manifestsAChosenCardFromHand() {
        Permanent scroll = harness.addToBattlefieldAndReturn(player1, new ScrollOfFate());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(scroll.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.isFaceDown()).isTrue();
                    assertThat(permanent.getCard()).isSameAs(bears);
                });
    }

    @Test
    void doesNothingWhenHandIsEmpty() {
        harness.addToBattlefield(player1, new ScrollOfFate());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
    }
}
