package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({Murmuration.class, AvenEnvoy.class, GrizzlyBears.class, DarkRitual.class})
class MurmurationTest extends BaseCardTest {

    @Test
    @DisplayName("Birds you control get +1/+1 and vigilance")
    void boostsBirdsYouControl() {
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new AvenEnvoy());
        Permanent nonBird = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBird = harness.addToBattlefieldAndReturn(player2, new AvenEnvoy());
        int birdPowerBefore = gqs.getEffectivePower(gd, bird);
        int birdToughnessBefore = gqs.getEffectiveToughness(gd, bird);

        harness.addToBattlefield(player1, new Murmuration());

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(birdPowerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(birdToughnessBefore + 1);
        assertThat(gqs.hasKeyword(gd, bird, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonBird)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonBird)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBird, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creates one Storm Crow for each spell cast this turn")
    void createsStormCrowsForSpellsCastThisTurn() {
        harness.addToBattlefield(player1, new Murmuration());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep(player1);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getName()).isEqualTo("Storm Crow");
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        });
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
