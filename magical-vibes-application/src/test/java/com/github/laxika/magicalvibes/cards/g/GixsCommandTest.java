package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GixsCommandTest extends BaseCardTest {

    @Test
    void countersAndLifelinkModeAffectsTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears firstGraveyardCreature = new GrizzlyBears();
        HillGiant secondGraveyardCreature = new HillGiant();
        Shock noncreature = new Shock();
        harness.setGraveyard(player1, List.of(firstGraveyardCreature, secondGraveyardCreature, noncreature));
        harness.setHand(player1, List.of(new GixsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2}, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
        harness.assertInHand(player1, firstGraveyardCreature.getName());
        harness.assertInHand(player1, secondGraveyardCreature.getName());
        harness.assertInGraveyard(player1, noncreature.getName());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void countersAndLifelinkModeCanBeCastWithoutChoosingACreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GixsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 1}, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void destroysCreaturesWithPowerTwoOrLess() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GixsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 2}, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void eachOpponentSacrificesTheirGreatestPowerCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new GixsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3}, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Serra Angel");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 5);
    }
}
