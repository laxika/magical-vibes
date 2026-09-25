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

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

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

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent fractal = findPermanent(player1, "Fractal");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, fractal.getId());
        harness.passBothPriorities();

        assertThat(fractal.isCantBeBlocked()).isTrue();
    }
}
