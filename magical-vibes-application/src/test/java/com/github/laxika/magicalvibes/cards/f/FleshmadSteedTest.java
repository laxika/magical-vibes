package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FleshmadSteedTest extends BaseCardTest {

    @Test
    @DisplayName("Fleshmad Steed taps when another creature dies")
    void tapsWhenAnotherCreatureDies() {
        Permanent steed = addReadySteed(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(steed.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Fleshmad Steed does not trigger when it dies")
    void doesNotTriggerWhenItDies() {
        harness.addToBattlefield(player1, new FleshmadSteed());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySteed(Player player) {
        Permanent permanent = new Permanent(new FleshmadSteed());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
