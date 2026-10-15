package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RallyToBattle.class, GrizzlyBears.class, Plains.class})
class RallyToBattleTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts and untaps your creatures only")
    void boostsAndUntapsYourCreaturesOnly() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownCreature.tap();
        ownLand.tap();
        opponentCreature.tap();

        cast();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(3);
        assertThat(ownLand.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getPowerModifier()).isZero();
        assertThat(opponentCreature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RallyToBattle()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(ownCreature.getToughnessModifier()).isZero();
        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolves with no creatures")
    void resolvesWithNoCreatures() {
        cast();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rally to Battle");
    }

    @Test
    @DisplayName("Affects creatures present at resolution, including untapped creatures")
    void affectsCreaturesPresentAtResolution() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RallyToBattle()));
        addMana();
        harness.castInstant(player1, 0);

        Permanent creatureBeforeResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creatureBeforeResolution.tap();
        harness.passBothPriorities();

        assertThat(existingCreature.getEffectivePower()).isEqualTo(3);
        assertThat(existingCreature.getEffectiveToughness()).isEqualTo(5);
        assertThat(existingCreature.isTapped()).isFalse();
        assertThat(creatureBeforeResolution.getEffectivePower()).isEqualTo(3);
        assertThat(creatureBeforeResolution.getEffectiveToughness()).isEqualTo(5);
        assertThat(creatureBeforeResolution.isTapped()).isFalse();

        Permanent creatureAfterResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creatureAfterResolution.tap();

        assertThat(creatureAfterResolution.getEffectivePower()).isEqualTo(2);
        assertThat(creatureAfterResolution.getEffectiveToughness()).isEqualTo(2);
        assertThat(creatureAfterResolution.isTapped()).isTrue();
    }

    private void cast() {
        harness.setHand(player1, List.of(new RallyToBattle()));
        addMana();
        harness.castAndResolveInstant(player1, 0);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
