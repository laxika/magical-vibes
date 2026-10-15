package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeekahFractalTheorist.class, GiantGrowth.class, BarkshellBlessing.class, GrizzlyBears.class})
class DeekahFractalTheoristTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant creates a Fractal with counters equal to its mana value")
    void castingInstantCreatesFractal() {
        addCreatureReady(player1, new DeekahFractalTheorist());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        Permanent fractal = findPermanent(player1, "Fractal");
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fractal.getEffectivePower()).isEqualTo(1);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Copying an instant creates another Fractal")
    void copyingInstantCreatesAnotherFractal() {
        addCreatureReady(player1, new DeekahFractalTheorist());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Fractal")).hasSize(2);
        assertThat(findPermanents(player1, "Fractal"))
                .allSatisfy(fractal -> assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    @DisplayName("The activated ability only targets creature tokens")
    void activatedAbilityOnlyTargetsCreatureTokens() {
        addCreatureReady(player1, new DeekahFractalTheorist());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activated ability makes a Fractal unblockable this turn")
    void activatedAbilityMakesFractalUnblockable() {
        addCreatureReady(player1, new DeekahFractalTheorist());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        Permanent fractal = findPermanent(player1, "Fractal");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, fractal.getId());
        harness.passBothPriorities();

        assertThat(fractal.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Casting an X sorcery includes the chosen X in the Fractal's counters")
    @CardUsed({DeekahFractalTheorist.class, DawnglowInfusion.class})
    void castingXSorceryIncludesXInManaValue() {
        addCreatureReady(player1, new DeekahFractalTheorist());
        harness.setHand(player1, List.of(new DawnglowInfusion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(findPermanents(player1, "Fractal")).singleElement()
                .satisfies(fractal -> assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(4));
    }

    @Test
    @DisplayName("Casting a creature does not trigger magecraft")
    void castingCreatureDoesNotCreateFractal() {
        addCreatureReady(player1, new DeekahFractalTheorist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Fractal")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's instant does not trigger magecraft")
    void opponentInstantDoesNotCreateFractal() {
        addCreatureReady(player1, new DeekahFractalTheorist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(findPermanents(player1, "Fractal")).isEmpty();
        assertThat(findPermanents(player2, "Fractal")).isEmpty();
    }

    @Test
    @DisplayName("The activated ability can target an opponent's creature token")
    void activatedAbilityCanTargetOpponentToken() {
        addCreatureReady(player1, new DeekahFractalTheorist());
        addCreatureReady(player2, new DeekahFractalTheorist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        Permanent fractal = findPermanent(player2, "Fractal");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, fractal.getId());
        harness.passBothPriorities();

        assertThat(fractal.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Doubling Season gives counters to both Fractals created by magecraft")
    @CardUsed({DeekahFractalTheorist.class, DoublingSeason.class, GrizzlyBears.class, GiantGrowth.class})
    void tokenDoublingPutsCountersOnEveryFractal() {
        addCreatureReady(player1, new DeekahFractalTheorist());
        harness.addToBattlefield(player1, new DoublingSeason());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Fractal")).hasSize(2)
                .allSatisfy(fractal -> assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(2));
    }
}
