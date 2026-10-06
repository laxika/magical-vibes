package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GallantCitizen;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SauronDinoDevotee.class, GallantCitizen.class})
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GallantCitizen());

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
        harness.castFromHand(player1, new SauronDinoDevotee(), "{3}{G}{G}");
        harness.passBothPriorities();
    }

    @Test
    void attacksAndTurnsAnotherCreatureIntoADinosaur() {
        Permanent sauron = harness.addToBattlefieldAndReturn(player1, new SauronDinoDevotee());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GallantCitizen());
        sauron.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

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
    void transformationPersistsAfterSauronLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GallantCitizen());
        transform(target);
        Permanent sauron = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, sauron));

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.DINOSAUR);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    void transformationEndsWhenLastSaurianCounterIsRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GallantCitizen());
        transform(target);

        target.setCounterCount(CounterType.SAURIAN, 0);

        assertCitizenCharacteristics(target);
    }

    @Test
    void replacingRemovedCounterDoesNotRestartExpiredTransformation() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GallantCitizen());
        transform(target);
        target.setCounterCount(CounterType.SAURIAN, 0);
        assertCitizenCharacteristics(target);

        target.setCounterCount(CounterType.SAURIAN, 1);

        assertCitizenCharacteristics(target);
    }

    @Test
    void saurianCounterOnUntargetedCreatureDoesNotTransformIt() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GallantCitizen());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GallantCitizen());
        other.setCounterCount(CounterType.SAURIAN, 1);

        transform(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertCitizenCharacteristics(other);
    }

    private void transform(Permanent target) {
        castSauron();
        harness.handleListChoice(player1, TURN_INTO_DINOSAURS);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void assertCitizenCharacteristics(Permanent target) {
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.CITIZEN);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }
}
