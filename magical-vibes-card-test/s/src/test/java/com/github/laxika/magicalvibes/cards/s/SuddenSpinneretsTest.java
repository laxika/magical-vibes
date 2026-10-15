package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EssenceSymbiote;
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

@CardUsed({SuddenSpinnerets.class, SleeperDart.class, EssenceSymbiote.class, HeartlessAct.class})
class SuddenSpinneretsTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts, untaps, and gives the target creature a reach counter")
    void boostsUntapsAndGivesReachCounter() {
        Permanent creature = addCreatureReady(player1, new EssenceSymbiote());
        creature.tap();

        castSuddenSpinnerets(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.REACH)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("The boost expires at end of turn but the reach counter remains")
    void boostExpiresButReachCounterRemains() {
        Permanent creature = addCreatureReady(player1, new EssenceSymbiote());
        creature.tap();
        castSuddenSpinnerets(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.REACH)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new SleeperDart());
        harness.setHand(player1, List.of(new SuddenSpinnerets()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can boost, give reach to, and untap an opposing creature")
    void canTargetOpposingCreature() {
        Permanent creature = addCreatureReady(player2, new EssenceSymbiote());
        creature.tap();

        castSuddenSpinnerets(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.REACH)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("An untapped creature still gets the boost and reach counter")
    void canTargetUntappedCreature() {
        Permanent creature = addCreatureReady(player1, new EssenceSymbiote());

        castSuddenSpinnerets(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.REACH)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Repeated casts stack the boosts and add a reach counter each time")
    void repeatedCastsAddCountersAndStackBoosts() {
        Permanent creature = addCreatureReady(player1, new EssenceSymbiote());

        castSuddenSpinnerets(creature);
        castSuddenSpinnerets(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
        assertThat(creature.getCounterCount(CounterType.REACH)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Does not resolve when its target is destroyed in response")
    void doesNotResolveWhenTargetLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new EssenceSymbiote());
        creature.tap();
        Permanent otherCreature = addCreatureReady(player1, new EssenceSymbiote());
        otherCreature.tap();
        harness.setHand(player1, List.of(new SuddenSpinnerets()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.setHand(player2, List.of(new HeartlessAct()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castModalInstant(player2, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, creature.getId())).isNull();
        harness.assertInGraveyard(player1, "Essence Symbiote");
        harness.assertInGraveyard(player1, "Sudden Spinnerets");
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
        assertThat(otherCreature.getCounterCount(CounterType.REACH)).isZero();
        assertThat(otherCreature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void castSuddenSpinnerets(Permanent target) {
        harness.setHand(player1, List.of(new SuddenSpinnerets()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
