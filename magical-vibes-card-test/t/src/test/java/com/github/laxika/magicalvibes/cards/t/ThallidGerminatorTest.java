package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({ThallidGerminator.class, GrizzlyBears.class, ArtificialEvolution.class, Bitterblossom.class})
class ThallidGerminatorTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a spore counter")
    void upkeepTriggerAddsSporeCounter() {
        Permanent thallid = addThallid();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing three spore counters creates a Saproling token")
    void removesThreeSporeCountersAndCreatesToken() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing a Saproling boosts a creature")
    void sacrificingSaprolingBoostsTargetCreature() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The token ability requires three spore counters")
    void tokenAbilityRequiresThreeSporeCounters() {
        addThallid().setCounterCount(CounterType.FUNGUS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost ability cannot sacrifice a non-Saproling creature")
    void boostAbilityRequiresSaproling() {
        addThallid();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost wears off during cleanup")
    void boostWearsOffAtEndOfTurn() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void opponentUpkeepDoesNotAddSporeCounter() {
        Permanent thallid = addThallid();

        advanceToUpkeep(player2);

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counterCostIsPaidBeforeTokenResolvesAndPreservesExtraCounters() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 5);

        harness.activateAbility(player1, 0, null, null);

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isEqualTo(2);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isEqualTo(2);
    }

    @Test
    void bothAbilitiesWorkWhileTappedAndSummoningSick() {
        Permanent thallid = harness.addToBattlefieldAndReturn(player1, new ThallidGerminator());
        thallid.setSummoningSick(true);
        thallid.tap();
        thallid.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, thallid.getId());

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(thallid.getPowerModifier()).isZero();

        harness.passBothPriorities();

        assertThat(thallid.getPowerModifier()).isEqualTo(1);
        assertThat(thallid.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void canTargetTheSaprolingSacrificedToPayTheCost() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent saproling = findPermanent(player1, "Saproling");

        harness.activateAbility(player1, 0, 1, null, saproling.getId());

        assertThat(findPermanents(player1, "Saproling")).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(thallid.getPowerModifier()).isZero();
        assertThat(thallid.getToughnessModifier()).isZero();
    }

    @Test
    void cannotSacrificeAnOpponentsSaproling() {
        Permanent thallid = addThallid();
        Permanent opponentThallid = harness.addToBattlefieldAndReturn(player2, new ThallidGerminator());
        opponentThallid.setCounterCount(CounterType.FUNGUS, 3);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, thallid.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player2, "Saproling")).hasSize(1);
    }

    @Test
    void canSacrificeANoncreatureSaprolingPermanent() {
        Permanent thallid = addThallid();
        Permanent bitterblossom = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, bitterblossom.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "SAPROLING");

        assertThat(gqs.hasEffectiveSubtype(gd, bitterblossom, CardSubtype.SAPROLING)).isTrue();

        harness.activateAbility(player1, 0, 1, null, thallid.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bitterblossom")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof Bitterblossom);
        assertThat(thallid.getPowerModifier()).isEqualTo(1);
        assertThat(thallid.getToughnessModifier()).isEqualTo(1);
    }

    private Permanent addThallid() {
        return addCreatureReady(player1, new ThallidGerminator());
    }
}
