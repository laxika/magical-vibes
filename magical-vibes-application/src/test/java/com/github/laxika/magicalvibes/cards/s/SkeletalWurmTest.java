package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkeletalWurmTest extends BaseCardTest {

    @Test
    void activatingRegenerationAbilityGrantsShield() {
        Permanent wurm = addWurmReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wurm.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void regenerationShieldSavesWurmFromDestroyEffect() {
        Permanent wurm = addWurmReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, wurm.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skeletal Wurm");
        assertThat(wurm.getRegenerationShield()).isZero();
    }

    private Permanent addWurmReady(Player player) {
        Permanent wurm = new Permanent(new SkeletalWurm());
        wurm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(wurm);
        return wurm;
    }
}
