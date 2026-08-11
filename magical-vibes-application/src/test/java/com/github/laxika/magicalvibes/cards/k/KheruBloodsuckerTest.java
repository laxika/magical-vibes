package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfEssence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KheruBloodsuckerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature with toughness 4 or greater drains each opponent and gains life")
    void sacrificingLargeCreatureDrainsOpponents() {
        Permanent bloodsucker = addKheruReady(player1);
        harness.addToBattlefield(player1, new WallOfEssence());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bloodsucker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Sacrificing a creature with toughness less than 4 does not drain or gain life")
    void sacrificingSmallCreatureDoesNotDrain() {
        Permanent bloodsucker = addKheruReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bloodsucker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The activated ability cannot sacrifice Kheru Bloodsucker itself")
    void activatedAbilityRequiresAnotherCreature() {
        addKheruReady(player1);
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addKheruReady(Player player) {
        Permanent permanent = new Permanent(new KheruBloodsucker());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
