package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WanderbrineTrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an opponent's creature after paying the ability's costs")
    void tapsOpponentCreature() {
        Permanent trapper = addReadyTrapper(player1);
        Permanent costCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(trapper.isTapped()).isTrue();
        assertThat(costCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot pay the cost by tapping Wanderbrine Trapper itself")
    void cannotTapSourceForAnotherCreatureCost() {
        addReadyTrapper(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by its controller")
    void rejectsControllerCreatureTarget() {
        addReadyTrapper(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyTrapper(Player player) {
        Permanent perm = new Permanent(new WanderbrineTrapper());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
