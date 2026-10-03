package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianBroodlings;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloatedProcessor.class, PhyrexianBroodlings.class, GrizzlyBears.class, FlameJavelin.class})
class BloatedProcessorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another Phyrexian puts a +1/+1 counter on Bloated Processor")
    void sacrificingAnotherPhyrexianPutsCounterOnIt() {
        Permanent processor = harness.addToBattlefieldAndReturn(player1, new BloatedProcessor());
        harness.addToBattlefield(player1, new PhyrexianBroodlings());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(processor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Phyrexian Broodlings");
    }

    @Test
    @DisplayName("It cannot sacrifice a non-Phyrexian for its ability")
    void cannotSacrificeNonPhyrexian() {
        harness.addToBattlefield(player1, new BloatedProcessor());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("When it dies, it incubates X where X is its power")
    void deathIncubatesItsPower() {
        Permanent processor = harness.addToBattlefieldAndReturn(player1, new BloatedProcessor());
        addCounterBySacrificingPhyrexian(processor);

        killWithFlameJavelin(processor);
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void cannotSacrificeItself() {
        harness.addToBattlefield(player1, new BloatedProcessor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Bloated Processor");
        harness.assertNotInGraveyard(player1, "Bloated Processor");
    }

    @Test
    void cannotSacrificeOpponentsPhyrexian() {
        harness.addToBattlefield(player1, new BloatedProcessor());
        harness.addToBattlefield(player2, new BloatedProcessor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Bloated Processor");
    }

    @Test
    void sacrificingProcessorIncubatesItsPowerBeforeTheOtherProcessorGrows() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new BloatedProcessor());
        harness.addToBattlefield(player1, new BloatedProcessor());

        harness.activateAbility(player1, 0, null, null);

        assertThat(countPermanents(player1, "Bloated Processor")).isEqualTo(1);
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Bloated Processor");

        resolveAllTriggers();

        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Incubator").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
    }

    @Test
    void incubatorFrontFaceHasIncubatorSubtype() {
        Permanent incubator = createIncubatorBySacrificingProcessor();

        assertThat(harness.getGameQueryService().isArtifact(gd, incubator)).isTrue();
        assertThat(harness.getGameQueryService().isCreature(gd, incubator)).isFalse();
        assertThat(incubator.getCard().getSubtypes()).extracting(Enum::name).contains("INCUBATOR");
    }

    @Test
    void transformingIncubatorPreservesCountersAndCreatesArtifactCreature() {
        Permanent incubator = createIncubatorBySacrificingProcessor();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(harness.getGameQueryService().isArtifact(gd, incubator)).isTrue();
        assertThat(harness.getGameQueryService().isCreature(gd, incubator)).isTrue();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, incubator)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, incubator)).isEqualTo(3);
    }

    @Test
    void transformedIncubatorHasStandardTokenName() {
        Permanent incubator = createIncubatorBySacrificingProcessor();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThat(incubator.getCard().getName()).isEqualTo("Phyrexian Token");
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 4})
    void nonpositivePowerStillCreatesIncubatorWithNoCounters(int minusCounters) {
        Permanent processor = harness.addToBattlefieldAndReturn(player1, new BloatedProcessor());
        processor.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, minusCounters);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Bloated Processor");
        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(harness.getGameQueryService().isCreature(gd, incubator)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private Permanent createIncubatorBySacrificingProcessor() {
        harness.addToBattlefield(player1, new BloatedProcessor());
        harness.addToBattlefield(player1, new BloatedProcessor());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        return findPermanent(player1, "Incubator");
    }

    private void addCounterBySacrificingPhyrexian(Permanent processor) {
        harness.addToBattlefield(player1, new PhyrexianBroodlings());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(processor), null, null);
        harness.passBothPriorities();
    }

    private void killWithFlameJavelin(Permanent processor) {
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, java.util.List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.castAndResolveInstant(player2, 0, processor.getId());
    }
}
