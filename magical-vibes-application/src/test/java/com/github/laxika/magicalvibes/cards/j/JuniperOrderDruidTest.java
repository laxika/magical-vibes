package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JuniperOrderDruid.class, Forest.class, BalduvianBears.class})
class JuniperOrderDruidTest extends BaseCardTest {

    private Permanent addTappedForest(com.github.laxika.magicalvibes.model.Player player) {
        Permanent forest = harness.addToBattlefieldAndReturn(player, new Forest());
        forest.tap();
        return forest;
    }

    @Test
    @DisplayName("Untaps target land")
    void untapsTargetLand() {
        Permanent druid = addCreatureReady(player1, new JuniperOrderDruid());
        Permanent forest = addTappedForest(player1);

        harness.activateAbility(player1, 0, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(druid.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can untap a land controlled by an opponent")
    void untapsOpponentLand() {
        addCreatureReady(player1, new JuniperOrderDruid());
        Permanent forest = addTappedForest(player2);

        harness.activateAbility(player1, 0, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetCreature() {
        addCreatureReady(player1, new JuniperOrderDruid());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());
        bears.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
