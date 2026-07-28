package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JuniperOrderDruidTest extends BaseCardTest {

    private Permanent addTappedForest(com.github.laxika.magicalvibes.model.Player player) {
        Permanent forest = new Permanent(new Forest());
        forest.tap();
        gd.playerBattlefields.get(player.getId()).add(forest);
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
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
