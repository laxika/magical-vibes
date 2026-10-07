package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DurableCoilbug;
import com.github.laxika.magicalvibes.cards.h.HeartlessAct;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({SpontaneousFlight.class, SleeperDart.class, HeartlessAct.class, DurableCoilbug.class})
class SpontaneousFlightTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts target creature and gives it a flying counter")
    void boostsAndGivesFlyingCounter() {
        Permanent creature = addCreatureReady(player1, new DurableCoilbug());

        castSpontaneousFlight(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(creature.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The boost expires at end of turn but the flying counter remains")
    void boostExpiresButFlyingCounterRemains() {
        Permanent creature = addCreatureReady(player1, new DurableCoilbug());
        castSpontaneousFlight(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new SleeperDart());
        harness.setHand(player1, List.of(new SpontaneousFlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can boost an opponent's creature without affecting another creature")
    void canTargetOpponentCreature() {
        Permanent ownCreature = addCreatureReady(player1, new DurableCoilbug());
        Permanent target = addCreatureReady(player2, new DurableCoilbug());

        castSpontaneousFlight(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(target.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(ownCreature.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Repeated casts accumulate boosts and flying counters")
    void repeatedCastsAccumulate() {
        Permanent target = addCreatureReady(player1, new DurableCoilbug());

        castSpontaneousFlight(target);
        castSpontaneousFlight(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        assertThat(target.getCounterCount(CounterType.FLYING)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Removing the flying counter removes flying without removing the boost")
    void removingCounterRemovesFlyingOnly() {
        Permanent target = addCreatureReady(player1, new DurableCoilbug());
        castSpontaneousFlight(target);
        harness.setHand(player1, List.of(new HeartlessAct()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1");

        assertThat(target.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does nothing when the target is destroyed in response")
    void targetDestroyedInResponse() {
        Permanent target = addCreatureReady(player1, new DurableCoilbug());
        Permanent otherCreature = addCreatureReady(player1, new DurableCoilbug());
        harness.setHand(player1, List.of(new SpontaneousFlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player2, List.of(new HeartlessAct()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player2, 0, 0, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Durable Coilbug");
        harness.assertInGraveyard(player1, "Spontaneous Flight");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
        assertThat(otherCreature.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
    }

    private void castSpontaneousFlight(Permanent target) {
        harness.setHand(player1, List.of(new SpontaneousFlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
