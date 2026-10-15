package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MinimusContainment;
import com.github.laxika.magicalvibes.cards.r.RelentlessAssault;
import com.github.laxika.magicalvibes.cards.s.Stifle;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({Berserk.class, GrizzlyBears.class, Forest.class, MinimusContainment.class,
        FountainOfYouth.class, Stifle.class, RelentlessAssault.class})
class BerserkTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles a creature's power and grants trample until end of turn")
    void boostsPowerAndGrantsTrample() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Berserk()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Destroys the target at the next end step if it attacked this turn")
    void destroysTargetThatAttackedThisTurn() {
        castBerserkOnTarget();
        declareAttackers(player1, List.of(0));

        resolveEndStep();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void delayedDestructionCanBeCountered() {
        Permanent target = castBerserkOnTarget();
        target.setAttackedThisTurn(true);
        harness.setHand(player1, List.of(new Stifle()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, gd.stack.getLast().getTargetableId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    void cannotTargetNonCreatureArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Berserk()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Does not destroy the target if it did not attack this turn")
    void preservesTargetThatDidNotAttack() {
        Permanent target = castBerserkOnTarget();

        resolveEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Destroys the target even if it stops being a creature before the end step")
    void destroysTargetThatStopsBeingACreature() {
        Permanent target = castBerserkOnTarget();
        target.setAttackedThisTurn(true);

        Permanent containment = harness.addToBattlefieldAndReturn(player1, new MinimusContainment());
        containment.setAttachedTo(target.getId());

        assertThat(gqs.isCreature(gd, target)).isFalse();
        resolveEndStep();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Berserk()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be cast from the combat damage step")
    void cannotCastFromCombatDamageStep() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.setHand(player1, List.of(new Berserk()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void cannotCastDuringAnAdditionalCombatAfterTheFirstCombat() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new Berserk()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void cannotCastInAnAdditionalCombatAfterTheFirstCombatWasSkipped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        gd.skipNextCombatPhaseCount.put(player1.getId(), 1);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new Berserk()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void usesPowerAtResolutionForEachBerserk() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Berserk(), new Berserk()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void negativePowerGivesNoPowerBoostButStillGrantsTrample() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setPowerModifier(-3);
        harness.setHand(player1, List.of(new Berserk()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void delayedAbilityTriggersEvenWhenTheCreatureDidNotAttack() {
        Permanent target = castBerserkOnTarget();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
    }

    @Test
    void illegalTargetAtResolutionDoesNotCreateDelayedDestruction() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setAttackedThisTurn(true);
        harness.setHand(player1, List.of(new Berserk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        Permanent containment = harness.addToBattlefieldAndReturn(player1, new MinimusContainment());
        containment.setAttachedTo(target.getId());

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Berserk");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    void cannotCastInPostcombatMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Berserk()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Permanent castBerserkOnTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Berserk()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        return target;
    }

    private void resolveEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);
    }
}
