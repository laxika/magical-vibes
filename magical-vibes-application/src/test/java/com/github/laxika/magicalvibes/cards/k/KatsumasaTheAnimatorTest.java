package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.c.CultivatorsCaravan;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KatsumasaTheAnimator.class, Arachnoid.class, ConjurersBauble.class,
        CultivatorsCaravan.class, SolRing.class})
class KatsumasaTheAnimatorTest extends BaseCardTest {

    @Test
    @DisplayName("Animates a non-Vehicle artifact into a 1/1 artifact creature with flying")
    void animatesNonVehicleArtifact() {
        addKatsumasa();
        Permanent bauble = harness.addToBattlefieldAndReturn(player1, new ConjurersBauble());

        activateAnimation(bauble);

        assertThat(gqs.isArtifact(gd, bauble)).isTrue();
        assertThat(gqs.isCreature(gd, bauble)).isTrue();
        assertThat(gqs.hasKeyword(gd, bauble, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bauble)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bauble)).isEqualTo(1);
    }

    @Test
    @DisplayName("Animates a Vehicle without changing its printed base power and toughness")
    void animatesVehicleWithoutChangingBasePowerToughness() {
        addKatsumasa();
        Permanent caravan = harness.addToBattlefieldAndReturn(player1, new CultivatorsCaravan());

        activateAnimation(caravan);

        assertThat(gqs.isCreature(gd, caravan)).isTrue();
        assertThat(gqs.hasKeyword(gd, caravan, Keyword.FLYING)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, caravan)).contains(CardSubtype.VEHICLE);
        assertThat(gqs.getEffectivePower(gd, caravan)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, caravan)).isEqualTo(5);
    }

    @Test
    @DisplayName("The animation ability only targets a noncreature artifact you control")
    void animationAbilityRejectsIllegalTargets() {
        addKatsumasa();
        Permanent opponentBauble = harness.addToBattlefieldAndReturn(player2, new ConjurersBauble());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBauble.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature artifact you control");
    }

    @Test
    @DisplayName("The upkeep trigger puts counters on up to three targeted noncreature artifacts")
    void upkeepTriggerPutsCountersOnTargets() {
        addKatsumasa();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ConjurersBauble());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ConjurersBauble());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new ConjurersBauble());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The upkeep trigger cannot target an artifact creature")
    void upkeepTriggerRejectsArtifactCreature() {
        addKatsumasa();
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());

        advanceToUpkeep(player1);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void animationRejectsArtifactCreaturesAndNonArtifacts() {
        Permanent katsumasa = addKatsumasa();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, katsumasa.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void upkeepCountersIncreaseAnimatedNonVehiclePowerAndToughness() {
        addKatsumasa();
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, ring.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();
        activateAnimation(ring);

        harness.assertOnBattlefield(player1, "Sol Ring");
        assertThat(gqs.isCreature(gd, ring)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ring)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ring)).isEqualTo(2);
    }

    @Test
    void upkeepMayChooseNoTargets() {
        addKatsumasa();
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(ring.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void upkeepCanChooseThreeArtifacts() {
        addKatsumasa();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new CultivatorsCaravan());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerOnOpponentsUpkeep() {
        addKatsumasa();
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(ring.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void upkeepSkipsTargetThatBecomesCreatureBeforeResolution() {
        addKatsumasa();
        Permanent caravan = harness.addToBattlefieldAndReturn(player1, new CultivatorsCaravan());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, caravan.getId());
        harness.handlePermanentChosen(player1, ring.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        activateAnimation(caravan);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, caravan)).isTrue();
        assertThat(caravan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ring.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void vehicleAnimationAndFlyingExpireAtEndOfTurn() {
        addKatsumasa();
        Permanent caravan = harness.addToBattlefieldAndReturn(player1, new CultivatorsCaravan());
        activateAnimation(caravan);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isArtifact(gd, caravan)).isTrue();
        assertThat(gqs.isCreature(gd, caravan)).isFalse();
        assertThat(gqs.hasKeyword(gd, caravan, Keyword.FLYING)).isFalse();
    }

    private Permanent addKatsumasa() {
        return harness.addToBattlefieldAndReturn(player1, new KatsumasaTheAnimator());
    }

    private void activateAnimation(Permanent target) {
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }
}
