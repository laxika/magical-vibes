package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutonomousAssemblerTest extends BaseCardTest {

    @Test
    void normalCastUsesPrintedSizeAndColor() {
        harness.setHand(player1, List.of(new AutonomousAssembler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent assembler = findPermanent(player1, "Autonomous Assembler");
        assertThat(gqs.getEffectivePower(gd, assembler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, assembler)).isEqualTo(5);
        assertThat(gqs.getEffectiveColors(gd, assembler)).isEmpty();
    }

    @Test
    void prototypeCastUsesAlternateSizeAndColor() {
        harness.setHand(player1, List.of(new AutonomousAssembler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();

        Permanent assembler = findPermanent(player1, "Autonomous Assembler");
        assertThat(gqs.getEffectivePower(gd, assembler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, assembler)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, assembler)).containsExactly(CardColor.WHITE);
    }

    @Test
    void activatedAbilityPutsCounterOnControlledAssemblyWorker() {
        Permanent assembler = addCreatureReady(player1, new AutonomousAssembler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, assembler.getId());
        harness.passBothPriorities();

        assertThat(assembler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(assembler.isTapped()).isTrue();
    }

    @Test
    void activatedAbilityCannotTargetOpponentAssemblyWorker() {
        Permanent assembler = addCreatureReady(player1, new AutonomousAssembler());
        Permanent opponentAssembler = addCreatureReady(player2, new AutonomousAssembler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentAssembler.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(assembler.isTapped()).isFalse();
    }
}
