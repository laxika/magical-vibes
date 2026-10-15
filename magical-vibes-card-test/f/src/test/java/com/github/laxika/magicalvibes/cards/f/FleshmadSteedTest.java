package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PharikasCure;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FleshmadSteed.class, CruelEdict.class, GrizzlyBears.class, PharikasCure.class})
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
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new FleshmadSteed());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    @DisplayName("A creature death triggers each surviving Steed, regardless of controller")
    void deathTriggersSteedsUnderBothControllers() {
        Permanent alliedSteed = harness.addToBattlefieldAndReturn(player1, new FleshmadSteed());
        Permanent opposingSteed = harness.addToBattlefieldAndReturn(player2, new FleshmadSteed());
        Permanent dyingSteed = harness.addToBattlefieldAndReturn(player1, new FleshmadSteed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new PharikasCure()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, dyingSteed.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dyingSteed);
        assertThat(gd.stack).hasSize(2);
        assertThat(alliedSteed.isTapped()).isFalse();
        assertThat(opposingSteed.isTapped()).isFalse();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(alliedSteed.isTapped()).isTrue();
        assertThat(opposingSteed.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already-tapped Steed still triggers and taps if untapped before resolution")
    void tappedSteedStillTriggers() {
        Permanent steed = harness.addToBattlefieldAndReturn(player1, new FleshmadSteed());
        steed.tap();
        Permanent dyingSteed = harness.addToBattlefieldAndReturn(player2, new FleshmadSteed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new PharikasCure()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, dyingSteed.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        steed.untap();
        harness.passBothPriorities();

        assertThat(steed.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
