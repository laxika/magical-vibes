package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NessianDemolokTest extends BaseCardTest {

    @Test
    @DisplayName("Paying tribute puts three +1/+1 counters on Nessian Demolok and preserves the target")
    void tributePaid() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent demolok = castDemolok(forest);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(demolok.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(forest.getId()));
    }

    @Test
    @DisplayName("Declining tribute destroys the targeted noncreature permanent")
    void tributeNotPaidDestroysTarget() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        castDemolok(forest);

        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(forest.getId()));
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Nessian Demolok cannot target a creature")
    void cannotTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castDemolok(Permanent target) {
        prepareCast();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Nessian Demolok");
    }

    private void prepareCast() {
        harness.setHand(player1, java.util.List.of(new NessianDemolok()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
