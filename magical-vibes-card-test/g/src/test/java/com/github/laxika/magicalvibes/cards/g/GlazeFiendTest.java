package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DregscapeZombie;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfEsper;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlazeFiend.class, DregscapeZombie.class, ObeliskOfEsper.class})
class GlazeFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Another artifact you control entering gives Glaze Fiend +2/+2")
    void allyArtifactEnterBoosts() {
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new GlazeFiend());

        harness.castFromHand(player1, new GlazeFiend(), "{1}{B}");
        harness.passBothPriorities(); // resolve spell, artifact enters, trigger onto stack
        harness.passBothPriorities(); // resolve trigger

        assertThat(fiend.getPowerModifier()).isEqualTo(2);
        assertThat(fiend.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtCleanup() {
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new GlazeFiend());

        harness.castFromHand(player1, new GlazeFiend(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(fiend.getPowerModifier()).isEqualTo(2);

        harness.setHand(player1, new ArrayList<>());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(fiend.getPowerModifier()).isEqualTo(0);
        assertThat(fiend.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("An artifact an opponent controls entering does not trigger Glaze Fiend")
    void opponentArtifactEnterDoesNotTrigger() {
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new GlazeFiend());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new GlazeFiend(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(fiend.getPowerModifier()).isEqualTo(0);
        assertThat(fiend.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Glaze Fiend does not trigger for its own entry")
    void ownEntryDoesNotTrigger() {
        harness.castFromHand(player1, new GlazeFiend(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent fiend = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(fiend.getPowerModifier()).isZero();
        assertThat(fiend.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A nonartifact creature entering does not trigger Glaze Fiend")
    void nonartifactEntryDoesNotTrigger() {
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new GlazeFiend());
        harness.castFromHand(player1, new DregscapeZombie(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(fiend.getPowerModifier()).isZero();
        assertThat(fiend.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Noncreature artifacts trigger Glaze Fiend and multiple boosts accumulate")
    void noncreatureArtifactBoostsAccumulate() {
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new GlazeFiend());

        harness.castFromHand(player1, new ObeliskOfEsper(), "{3}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(fiend.getPowerModifier()).isZero();
        assertThat(fiend.getToughnessModifier()).isZero();
        harness.passBothPriorities();
        assertThat(fiend.getPowerModifier()).isEqualTo(2);
        assertThat(fiend.getToughnessModifier()).isEqualTo(2);

        harness.castFromHand(player1, new ObeliskOfEsper(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(fiend.getPowerModifier()).isEqualTo(4);
        assertThat(fiend.getToughnessModifier()).isEqualTo(4);
    }
}
