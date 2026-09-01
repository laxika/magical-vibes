package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AerialManeuver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpellwildOuphe.class, AerialManeuver.class, GrizzlyBears.class})
class SpellwildOupheTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces the cost of a controller's spell targeting it")
    void reducesControllersSpellTargetingIt() {
        Permanent ouphe = harness.addToBattlefieldAndReturn(player1, new SpellwildOuphe());
        harness.setHand(player1, List.of(new AerialManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, ouphe.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Reduces the cost of an opponent's spell targeting it")
    void reducesOpponentsSpellTargetingIt() {
        Permanent ouphe = harness.addToBattlefieldAndReturn(player1, new SpellwildOuphe());
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AerialManeuver()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, ouphe.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not reduce a spell targeting another creature")
    void doesNotReduceSpellTargetingAnotherCreature() {
        harness.addToBattlefield(player1, new SpellwildOuphe());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AerialManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
