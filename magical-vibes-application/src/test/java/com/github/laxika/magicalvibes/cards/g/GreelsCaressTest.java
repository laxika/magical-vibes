package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.cards.r.RidgelineRager;
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

@CardUsed({GreelsCaress.class, RidgelineRager.class, RhysticCave.class})
class GreelsCaressTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -3/-0")
    void reducesEnchantedCreaturePower() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RidgelineRager());

        harness.setHand(player1, List.of(new GreelsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can enchant a creature an opponent controls")
    void canEnchantOpponentsCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RidgelineRager());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RidgelineRager());

        harness.setHand(player1, List.of(new GreelsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.castEnchantment(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can be cast during an opponent's turn because of flash")
    void canBeCastDuringOpponentsTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RidgelineRager());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GreelsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.passPriority(player2);

        harness.castEnchantment(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new RidgelineRager());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());

        harness.setHand(player1, List.of(new GreelsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
