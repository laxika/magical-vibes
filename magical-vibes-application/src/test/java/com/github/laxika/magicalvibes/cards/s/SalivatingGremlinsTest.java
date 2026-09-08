package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SalivatingGremlinsTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact you control entering gives Salivating Gremlins +2/+0 and trample")
    void allyArtifactEnterBoostsAndGrantsTrample() {
        Permanent gremlins = harness.addToBattlefieldAndReturn(player1, new SalivatingGremlins());

        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
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

        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
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
        harness.setHand(player2, List.of(new GlazeFiend()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gremlins.getPowerModifier()).isEqualTo(0);
        assertThat(gremlins.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }
}
