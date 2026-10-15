package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SwordwiseCentaur;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NessianDemolok.class, Forest.class, SwordwiseCentaur.class})
class NessianDemolokTest extends BaseCardTest {

    @Test
    @DisplayName("Paying tribute puts three +1/+1 counters on Nessian Demolok and preserves the target")
    void tributePaid() {
        harness.addToBattlefield(player2, new Forest());
        Permanent demolok = castDemolok();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(demolok.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining tribute destroys the targeted noncreature permanent")
    void tributeNotPaidDestroysTarget() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent demolok = castDemolok();

        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(demolok.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Nessian Demolok cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        castDemolok();
        harness.handleMayAbilityChosen(player2, false);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Swordwise Centaur");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Nessian Demolok can enter and receive tribute with no legal destruction targets")
    void tributePaidWithoutNoncreaturePermanents() {
        Permanent demolok = castDemolok();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(demolok.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Nessian Demolok");
    }

    @Test
    @DisplayName("Declining tribute with no legal destruction targets leaves Nessian Demolok on the battlefield")
    void tributeDeclinedWithoutNoncreaturePermanents() {
        Permanent demolok = castDemolok();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(demolok.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Nessian Demolok");
    }

    @Test
    @DisplayName("Declining tribute can destroy a noncreature permanent controlled by Nessian Demolok's controller")
    void tributeDeclinedDestroysOwnPermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castDemolok();
        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Nessian Demolok");
    }

    private Permanent castDemolok() {
        prepareCast();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Nessian Demolok");
    }

    private void prepareCast() {
        harness.setHand(player1, java.util.List.of(new NessianDemolok()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
