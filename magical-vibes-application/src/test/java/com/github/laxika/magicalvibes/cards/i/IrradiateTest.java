package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FangrenHunter;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Irradiate.class, FangrenHunter.class, Ornithopter.class, Forest.class})
class IrradiateTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets -1/-1 for each artifact you control")
    void shrinksForEachControlledArtifact() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new FangrenHunter());
        harness.setHand(player1, List.of(new Irradiate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID hunterId = harness.getPermanentId(player2, "Fangren Hunter");
        harness.castInstant(player1, 0, hunterId);
        harness.passBothPriorities();

        Permanent hunter = findPermanent(player2, "Fangren Hunter");
        assertThat(hunter.getEffectivePower()).isEqualTo(2);
        assertThat(hunter.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The -1/-1 effect wears off at cleanup")
    void shrinkWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new FangrenHunter());
        harness.setHand(player1, List.of(new Irradiate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID hunterId = harness.getPermanentId(player2, "Fangren Hunter");
        harness.castInstant(player1, 0, hunterId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent hunter = findPermanent(player2, "Fangren Hunter");
        assertThat(hunter.getEffectivePower()).isEqualTo(4);
        assertThat(hunter.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Irradiate cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Irradiate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID forestId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Irradiate can target your own creature and does nothing with no artifacts")
    void canTargetOwnCreatureWithNoArtifacts() {
        harness.addToBattlefield(player1, new FangrenHunter());
        harness.setHand(player1, List.of(new Irradiate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID hunterId = harness.getPermanentId(player1, "Fangren Hunter");
        harness.castInstant(player1, 0, hunterId);
        harness.passBothPriorities();

        Permanent hunter = findPermanent(player1, "Fangren Hunter");
        assertThat(hunter.getEffectivePower()).isEqualTo(4);
        assertThat(hunter.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Irradiate counts artifacts when it resolves")
    void countsArtifactsAtResolution() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new FangrenHunter());
        harness.setHand(player1, List.of(new Irradiate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID hunterId = harness.getPermanentId(player2, "Fangren Hunter");
        harness.castInstant(player1, 0, hunterId);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.passBothPriorities();

        Permanent hunter = findPermanent(player2, "Fangren Hunter");
        assertThat(hunter.getEffectivePower()).isEqualTo(2);
        assertThat(hunter.getEffectiveToughness()).isEqualTo(2);
    }
}
