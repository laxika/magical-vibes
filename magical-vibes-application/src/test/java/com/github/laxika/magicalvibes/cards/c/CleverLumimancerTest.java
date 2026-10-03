package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.e.ExpandedAnatomy;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CleverLumimancer.class, BarkshellBlessing.class, ExpandedAnatomy.class, GiantGrowth.class, GrizzlyBears.class})
class CleverLumimancerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant boosts Clever Lumimancer until end of turn")
    void castingInstantBoostsLumimancer() {
        Permanent lumimancer = addCreatureReady(player1, new CleverLumimancer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(lumimancer.getEffectivePower()).isEqualTo(2);
        assertThat(lumimancer.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Copying an instant triggers Clever Lumimancer")
    void copyingInstantBoostsLumimancer() {
        Permanent lumimancer = addCreatureReady(player1, new CleverLumimancer());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(lumimancer.getEffectivePower()).isEqualTo(4);
        assertThat(lumimancer.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Magecraft boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent lumimancer = addCreatureReady(player1, new CleverLumimancer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();
        assertThat(lumimancer.getEffectivePower()).isEqualTo(2);
        assertThat(lumimancer.getEffectiveToughness()).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(lumimancer.getEffectivePower()).isZero();
        assertThat(lumimancer.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a sorcery triggers magecraft before the spell resolves")
    void castingSorceryBoostsLumimancer() {
        Permanent lumimancer = addCreatureReady(player1, new CleverLumimancer());
        harness.setHand(player1, List.of(new ExpandedAnatomy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, lumimancer.getId());

        assertThat(lumimancer.getEffectivePower()).isEqualTo(2);
        assertThat(lumimancer.getEffectiveToughness()).isEqualTo(3);
        resolveAllTriggers();
        assertThat(lumimancer.getEffectivePower()).isEqualTo(4);
        assertThat(lumimancer.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent's instant does not trigger magecraft")
    void opponentInstantDoesNotBoostLumimancer() {
        Permanent lumimancer = addCreatureReady(player1, new CleverLumimancer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.ensurePriority(player2);

        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(lumimancer.getEffectivePower()).isZero();
        assertThat(lumimancer.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature does not trigger magecraft")
    void creatureSpellDoesNotBoostLumimancer() {
        Permanent lumimancer = addCreatureReady(player1, new CleverLumimancer());
        harness.setHand(player1, List.of(new CleverLumimancer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(lumimancer.getEffectivePower()).isZero();
        assertThat(lumimancer.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple instant casts accumulate separate magecraft boosts")
    void multipleCastsAccumulateBoosts() {
        Permanent lumimancer = addCreatureReady(player1, new CleverLumimancer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(lumimancer.getEffectivePower()).isEqualTo(4);
        assertThat(lumimancer.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent's cast and conspire copy do not trigger magecraft")
    void opponentCopyDoesNotBoostLumimancer() {
        Permanent controllerLumimancer = addCreatureReady(player1, new CleverLumimancer());
        Permanent opponentLumimancer = addCreatureReady(player2, new CleverLumimancer());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, conspireA.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(controllerLumimancer.getEffectivePower()).isEqualTo(4);
        assertThat(controllerLumimancer.getEffectiveToughness()).isEqualTo(5);
        assertThat(opponentLumimancer.getEffectivePower()).isZero();
        assertThat(opponentLumimancer.getEffectiveToughness()).isEqualTo(1);
    }
}
