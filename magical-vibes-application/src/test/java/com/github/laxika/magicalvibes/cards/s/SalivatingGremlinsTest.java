package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CogworkersPuzzleknot;
import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SalivatingGremlins.class, GlazeFiend.class, CogworkersPuzzleknot.class})
class SalivatingGremlinsTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact you control entering gives Salivating Gremlins +2/+0 and trample")
    void allyArtifactEnterBoostsAndGrantsTrample() {
        Permanent gremlins = harness.addToBattlefieldAndReturn(player1, new SalivatingGremlins());

        harness.castFromHand(player1, new GlazeFiend(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gremlins.getPowerModifier()).isEqualTo(2);
        assertThat(gremlins.getToughnessModifier()).isEqualTo(0);
        assertThat(gremlins.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void boostAndTrampleWearOffAtCleanup() {
        Permanent gremlins = harness.addToBattlefieldAndReturn(player1, new SalivatingGremlins());

        harness.castFromHand(player1, new GlazeFiend(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, new ArrayList<>());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gremlins.getPowerModifier()).isEqualTo(0);
        assertThat(gremlins.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An artifact an opponent controls entering does not trigger Salivating Gremlins")
    void opponentArtifactEnterDoesNotTrigger() {
        Permanent gremlins = harness.addToBattlefieldAndReturn(player1, new SalivatingGremlins());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new GlazeFiend(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gremlins.getPowerModifier()).isEqualTo(0);
        assertThat(gremlins.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The boost and trample resolve together as one triggered ability")
    void boostAndTrampleAreOneTriggeredAbility() {
        Permanent gremlins = harness.addToBattlefieldAndReturn(player1, new SalivatingGremlins());

        harness.castFromHand(player1, new GlazeFiend(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gremlins.getPowerModifier()).isZero();
        assertThat(gremlins.hasKeyword(Keyword.TRAMPLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gremlins.getPowerModifier()).isEqualTo(2);
        assertThat(gremlins.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A nonartifact creature entering does not trigger the ability")
    void nonartifactCreatureDoesNotTrigger() {
        Permanent gremlins = harness.addToBattlefieldAndReturn(player1, new SalivatingGremlins());

        harness.castFromHand(player1, new SalivatingGremlins(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gremlins.getPowerModifier()).isZero();
        assertThat(gremlins.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Both a noncreature artifact and its Servo token trigger the ability")
    void artifactAndArtifactTokenEachTrigger() {
        Permanent gremlins = harness.addToBattlefieldAndReturn(player1, new SalivatingGremlins());

        harness.castFromHand(player1, new CogworkersPuzzleknot(), "{2}");
        for (int i = 0; i < 10 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gremlins.getPowerModifier()).isEqualTo(4);
        assertThat(gremlins.getToughnessModifier()).isZero();
        assertThat(gremlins.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }
}
