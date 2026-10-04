package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantsGrasp.class, HillGiant.class, GrizzlyBears.class, Plains.class, Disperse.class})
class GiantsGraspTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, gains control of a target nonland permanent while it remains on the battlefield")
    void gainsControlOfTargetNonlandPermanent() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        Permanent grasp = castAndResolve(giant, target);

        assertThat(grasp.getAttachedTo()).isEqualTo(giant.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("The ETB ability cannot target a land")
    void cannotTargetLandWithEtbAbility() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.setHand(player1, List.of(new GiantsGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("It can enchant only a Giant controlled by its controller")
    void requiresControlledGiantAsEnchantmentTarget() {
        Permanent nonGiant = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentGiant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new GiantsGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonGiant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Giant you control");
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentGiant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Giant you control");
    }

    @Test
    @DisplayName("The stolen permanent returns to its owner when the Aura leaves")
    void controlEndsWhenAuraLeaves() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent grasp = castAndResolve(giant, target);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, grasp.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    private Permanent castAndResolve(Permanent giant, Permanent target) {
        harness.setHand(player1, List.of(new GiantsGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GiantsGrasp)
                .findFirst()
                .orElseThrow();
    }
}
