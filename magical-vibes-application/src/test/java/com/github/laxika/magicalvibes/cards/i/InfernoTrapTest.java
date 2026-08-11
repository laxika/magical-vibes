package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InfernoTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature")
    void dealsDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new InfernoTrap()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Inferno Trap");
    }

    @Test
    @DisplayName("Can be cast for {R} after two creatures dealt damage this turn")
    void castsForAlternateCostAfterTwoCreaturesDealDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new InfernoTrap()));
        harness.addMana(player1, ManaColor.RED, 1);
        addReadyCreature(player2, new GrizzlyBears());
        addReadyCreature(player2, new GrizzlyBears());
        declareAttackers(player2, List.of(0, 1));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Inferno Trap");
    }

    @Test
    @DisplayName("One creature dealing damage does not enable the alternate cost")
    void alternateCostRequiresTwoDistinctCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        addReadyCreature(player2, new HillGiant());
        declareAttackers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        harness.setHand(player1, List.of(new InfernoTrap()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                  com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
    }
}
