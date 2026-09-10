package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyScourer.class, Ornithopter.class, GrizzlyBears.class})
class SkyScourerTest extends BaseCardTest {

    private Permanent addSkyScourer() {
        harness.addToBattlefield(player1, new SkyScourer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting a colorless spell gives Sky Scourer +1/+0 until end of turn")
    void colorlessSpellPumps() {
        Permanent scourer = addSkyScourer();

        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scourer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scourer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a colored spell does not trigger Sky Scourer")
    void coloredSpellDoesNotPump() {
        Permanent scourer = addSkyScourer();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scourer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scourer)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Sky Scourer boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent scourer = addSkyScourer();

        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scourer)).isEqualTo(2);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, scourer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scourer)).isEqualTo(2);
    }
}
