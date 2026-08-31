package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaruFistOfKrosa.class, Forest.class, GrizzlyBears.class, HillGiant.class, Island.class})
class BaruFistOfKrosaTest extends BaseCardTest {

    @Test
    @DisplayName("A Forest boosts green creatures you control and grants them trample until end of turn")
    void forestBoostsGreenCreaturesAndGrantsTrample() {
        Permanent baru = harness.addToBattlefieldAndReturn(player1, new BaruFistOfKrosa());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int hillGiantPower = gqs.getEffectivePower(gd, hillGiant);
        int opponentBearsPower = gqs.getEffectivePower(gd, opponentBears);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, baru)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, baru)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, baru, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, hillGiant)).isEqualTo(hillGiantPower);
        assertThat(gqs.hasKeyword(gd, hillGiant, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(opponentBearsPower);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, baru)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, baru, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Forest also triggers Baru")
    void opponentsForestTriggersBaru() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.addToBattlefield(player1, new BaruFistOfKrosa());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A non-Forest land does not trigger Baru")
    void nonForestDoesNotTriggerBaru() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BaruFistOfKrosa());
        harness.setHand(player1, List.of(new Island()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Grandeur creates a Wurm whose size equals your land count")
    void grandeurCreatesWurmBasedOnLandCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new BaruFistOfKrosa());
        harness.setHand(player1, List.of(new BaruFistOfKrosa()));

        harness.activateAbility(player1, 3, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent wurm = findPermanent(player1, "Wurm");
        assertThat(wurm.getEffectivePower()).isEqualTo(3);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
