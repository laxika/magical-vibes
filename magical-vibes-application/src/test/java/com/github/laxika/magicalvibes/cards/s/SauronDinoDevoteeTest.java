package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SauronDinoDevotee.class, GrizzlyBears.class})
class SauronDinoDevoteeTest extends BaseCardTest {

    private static final String CURE_CANCER = "Cure Cancer — You gain 3 life.";
    private static final String TURN_INTO_DINOSAURS = "Turn People into Dinosaurs — Put a saurian counter on another target creature. It's a green Dinosaur with base power and toughness 5/5 for as long as it has a saurian counter on it.";

    @Test
    void entersAndGainsThreeLife() {
        int lifeBefore = gd.getLife(player1.getId());

        castSauron();
        harness.handleListChoice(player1, CURE_CANCER);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    void entersAndTurnsAnotherCreatureIntoADinosaur() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSauron();
        harness.handleListChoice(player1, TURN_INTO_DINOSAURS);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.SAURIAN)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.DINOSAUR);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    void attacksAndGainsThreeLife() {
        Permanent sauron = harness.addToBattlefieldAndReturn(player1, new SauronDinoDevotee());
        sauron.setSummoningSick(false);
        int lifeBefore = gd.getLife(player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        harness.handleListChoice(player1, CURE_CANCER);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    private void castSauron() {
        harness.setHand(player1, List.of(new SauronDinoDevotee()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
