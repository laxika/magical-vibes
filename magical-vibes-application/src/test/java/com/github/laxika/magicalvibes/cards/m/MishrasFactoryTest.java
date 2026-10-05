package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MishrasFactory.class, GrizzlyBears.class})
class MishrasFactoryTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Mishra's Factory produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent factory = addFactoryReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(factory);

        gs.tapPermanent(gd, player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("{1} makes it a 2/2 Assembly-Worker artifact creature that's still a land")
    void animateMakesItACreature() {
        Permanent factory = addFactoryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, factory)).isTrue();
        assertThat(gqs.isArtifact(factory)).isTrue();
        assertThat(gqs.getEffectivePower(gd, factory)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, factory)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, factory)).contains(CardSubtype.ASSEMBLY_WORKER);
        assertThat(gqs.isLand(gd, factory)).isTrue();
    }

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent factory = addFactoryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, factory)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(factory.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, factory)).isFalse();
        assertThat(gqs.isArtifact(factory)).isFalse();
    }

    @Test
    @DisplayName("{T} pump gives an animated Mishra's Factory +1/+1")
    void pumpBoostsAssemblyWorker() {
        Permanent factory = addFactoryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Animate it so it is a legal Assembly-Worker creature target.
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // {T}: it targets itself as an Assembly-Worker creature.
        harness.activateAbility(player1, 0, 1, null, factory.getId());
        harness.passBothPriorities();

        assertThat(factory.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, factory)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, factory)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("{T} pump cannot target a non-Assembly-Worker creature")
    void pumpCannotTargetNonAssemblyWorker() {
        addFactoryReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void pumpResetsAtEndOfTurn() {
        Permanent factory = addFactoryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, factory.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, factory)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, factory)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, factory)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, factory)).isEqualTo(2);
    }

    private Permanent addFactoryReady(Player player) {
        return addCreatureReady(player, new MishrasFactory());
    }

    @Test
    void unanimatedFactoryCanPumpOpponentsAssemblyWorker() {
        Permanent source = addFactoryReady(player1);
        Permanent target = addFactoryReady(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, source)).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void tappedFactoryCanAnimateWithoutUntappingOrLosingManaAbility() {
        Permanent factory = addFactoryReady(player1);
        harness.tapPermanent(player1, 0);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(factory.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, factory)).isTrue();
        assertThat(gqs.getEffectivePower(gd, factory)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        factory.setTapped(false);
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void newlyEnteredFactoryCanAnimateButCannotPayCreatureTapCosts() {
        Permanent factory = harness.addToBattlefieldAndReturn(player1, new MishrasFactory());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, factory)).isTrue();
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, factory.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(factory.isTapped()).isFalse();
    }

    @Test
    void newlyEnteredUnanimatedFactoryCanTapForMana() {
        Permanent factory = harness.addToBattlefieldAndReturn(player1, new MishrasFactory());

        harness.tapPermanent(player1, 0);

        assertThat(factory.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void animatingAgainPreservesPumpAndDoesNotUntapFactory() {
        Permanent factory = addFactoryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, factory.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(factory.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, factory)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, factory)).isEqualTo(3);
    }

    @Test
    void pumpCannotTargetUnanimatedFactory() {
        addFactoryReady(player1);
        Permanent target = addFactoryReady(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
