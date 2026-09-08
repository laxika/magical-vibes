package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MandibleJusticiarTest extends BaseCardTest {

    @Test
    @DisplayName("Another artifact you control entering gives Mandible Justiciar +1/+1")
    void allyArtifactEnterBoosts() {
        Permanent justiciar = harness.addToBattlefieldAndReturn(player1, new MandibleJusticiar());

        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(justiciar.getPowerModifier()).isEqualTo(1);
        assertThat(justiciar.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtCleanup() {
        Permanent justiciar = harness.addToBattlefieldAndReturn(player1, new MandibleJusticiar());

        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(justiciar.getPowerModifier()).isEqualTo(1);

        harness.setHand(player1, new ArrayList<>());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(justiciar.getPowerModifier()).isEqualTo(0);
        assertThat(justiciar.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("An artifact an opponent controls entering does not trigger Mandible Justiciar")
    void opponentArtifactEnterDoesNotTrigger() {
        Permanent justiciar = harness.addToBattlefieldAndReturn(player1, new MandibleJusticiar());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GlazeFiend()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(justiciar.getPowerModifier()).isEqualTo(0);
        assertThat(justiciar.getToughnessModifier()).isEqualTo(0);
    }
}
