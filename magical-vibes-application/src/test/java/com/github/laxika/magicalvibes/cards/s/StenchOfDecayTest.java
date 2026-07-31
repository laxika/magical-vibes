package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StenchOfDecayTest extends BaseCardTest {

    @Test
    @DisplayName("Nonartifact creatures on both sides get -1/-1, artifact creatures unaffected")
    void shrinksNonartifactCreaturesOnly() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent thopter = addCreatureReady(player2, new Ornithopter());

        castStench();

        assertThat(ownBear.getEffectivePower()).isEqualTo(1);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(1);
        assertThat(opponentBear.getEffectivePower()).isEqualTo(1);
        assertThat(opponentBear.getEffectiveToughness()).isEqualTo(1);
        assertThat(thopter.getEffectivePower()).isEqualTo(0);
        assertThat(thopter.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("1/1 creatures die to the -1/-1")
    void onlyOneToughnessCreaturesDie() {
        Permanent wizard = addCreatureReady(player2, new FugitiveWizard());

        castStench();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(wizard);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        castStench();

        assertThat(bear.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    private void castStench() {
        harness.setHand(player1, List.of(new StenchOfDecay()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0);
    }
}
