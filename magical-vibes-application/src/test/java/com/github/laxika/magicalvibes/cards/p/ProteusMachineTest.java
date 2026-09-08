package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ProteusMachine.class)
class ProteusMachineTest extends BaseCardTest {

    @Test
    void becomesTheChosenCreatureTypeIndefinitelyWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new ProteusMachine()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent machine = findPermanent(player1, "Proteus Machine");
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(machine));
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.hasEffectiveSubtype(gd, machine, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, machine, CardSubtype.SHAPESHIFTER)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, machine, CardSubtype.GOBLIN)).isTrue();
    }
}
