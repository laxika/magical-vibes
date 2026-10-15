package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({CompoundFracture.class, FountainOfYouth.class, HillGiant.class})
class CompoundFractureTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -1/-1 with no Compound Fractures in the graveyard")
    void givesBaseDebuffWithEmptyGraveyard() {
        Permanent target = addCreature();
        castCompoundFracture(target);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Gives an additional -1/-1 for each Compound Fracture in the controller's graveyard")
    void debuffScalesWithNamedCardsInGraveyard() {
        Permanent target = addCreature();
        harness.setGraveyard(player1, List.of(new CompoundFracture(), new CompoundFracture(), new FountainOfYouth()));
        castCompoundFracture(target);

        assertThat(target.getEffectivePower()).isEqualTo(0);
        assertThat(target.getEffectiveToughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Counts only the controller's graveyard")
    void ignoresOpponentGraveyard() {
        Permanent target = addCreature();
        harness.setGraveyard(player2, List.of(new CompoundFracture(), new CompoundFracture()));
        castCompoundFracture(target);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Debuff wears off at cleanup")
    void debuffWearsOffAtCleanup() {
        Permanent target = addCreature();
        castCompoundFracture(target);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new CompoundFracture()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Later resolutions count an earlier resolved Compound Fracture, but not themselves")
    void countsGraveyardAtResolution() {
        Permanent firstTarget = addCreature();
        Permanent secondTarget = addCreature();
        harness.setHand(player1, List.of(new CompoundFracture(), new CompoundFracture()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, firstTarget.getId());
        harness.castInstant(player1, 0, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(secondTarget.getEffectivePower()).isEqualTo(2);
        assertThat(secondTarget.getEffectiveToughness()).isEqualTo(2);
        assertThat(firstTarget.getEffectiveToughness()).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(firstTarget.getEffectivePower()).isEqualTo(1);
        assertThat(firstTarget.getEffectiveToughness()).isEqualTo(1);
        assertThat(secondTarget.getEffectivePower()).isEqualTo(2);
        assertThat(secondTarget.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The resolved debuff stays fixed when named cards leave the graveyard")
    void resolvedDebuffDoesNotRecountGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new CompoundFracture()));
        castCompoundFracture(target);

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.setGraveyard(player1, List.of());

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature reduced to zero toughness goes to its owner's graveyard")
    void lethalDebuffPutsCreatureInGraveyard() {
        Permanent target = addCreature();
        harness.setGraveyard(player1, List.of(new CompoundFracture(), new CompoundFracture()));

        castCompoundFracture(target);

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    private Permanent addCreature() {
        return harness.addToBattlefieldAndReturn(player2, new HillGiant());
    }

    private void castCompoundFracture(Permanent target) {
        harness.setHand(player1, List.of(new CompoundFracture()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
