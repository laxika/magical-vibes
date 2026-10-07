package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StegronTheDinosaurMan.class, GrizzlyBears.class})
class StegronTheDinosaurManTest extends BaseCardTest {

    @Test
    @DisplayName("Dinosaur Formula gives a creature +3/+1 and Dinosaur until end of turn")
    void dinosaurFormulaBoostsAndAddsDinosaurType() {
        harness.setHand(player1, List.of(new StegronTheDinosaurMan()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears))
                .containsExactlyInAnyOrder(CardSubtype.BEAR, CardSubtype.DINOSAUR);
        harness.assertInGraveyard(player1, "Stegron the Dinosaur Man");
    }

    @Test
    @DisplayName("Dinosaur Formula wears off at end of turn")
    void dinosaurFormulaWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new StegronTheDinosaurMan()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).containsExactly(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Dinosaur Formula cannot target an opponent's creature")
    void dinosaurFormulaRejectsOpponentCreature() {
        harness.setHand(player1, List.of(new StegronTheDinosaurMan()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Stegron the Dinosaur Man");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dinosaur Formula discards as a cost and can be activated on an opponent's turn")
    void dinosaurFormulaCanBeActivatedOnOpponentsTurn() {
        harness.setHand(player1, List.of(new StegronTheDinosaurMan()));
        Permanent stegron = harness.addToBattlefieldAndReturn(player1, new StegronTheDinosaurMan());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, stegron.getId());

        harness.assertNotInHand(player1, "Stegron the Dinosaur Man");
        harness.assertInGraveyard(player1, "Stegron the Dinosaur Man");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, stegron)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, stegron)).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stegron)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, stegron)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, stegron))
                .containsExactlyInAnyOrder(CardSubtype.DINOSAUR, CardSubtype.VILLAIN);
    }

    @Test
    @DisplayName("Dinosaur Formula requires red mana and does not discard when its cost cannot be paid")
    void dinosaurFormulaRejectsInsufficientColoredMana() {
        harness.setHand(player1, List.of(new StegronTheDinosaurMan()));
        Permanent stegron = harness.addToBattlefieldAndReturn(player1, new StegronTheDinosaurMan());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, stegron.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Stegron the Dinosaur Man");
        harness.assertNotInGraveyard(player1, "Stegron the Dinosaur Man");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, stegron)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, stegron)).isEqualTo(4);
    }
}
