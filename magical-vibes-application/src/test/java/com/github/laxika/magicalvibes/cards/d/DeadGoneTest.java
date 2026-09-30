package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SerraSphinx;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadGone.class, SerraSphinx.class})
class DeadGoneTest extends BaseCardTest {

    @Test
    void deadDealsTwoDamageToTargetCreature() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player2, new SerraSphinx());

        harness.setHand(player1, List.of(new DeadGone()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castModalInstant(player1, 0, 0, List.of(sphinx.getId()));
        harness.passBothPriorities();

        assertThat(sphinx.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void deadCanTargetACreatureYouControl() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SerraSphinx());

        harness.setHand(player1, List.of(new DeadGone()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castModalInstant(player1, 0, 0, List.of(sphinx.getId()));
        harness.passBothPriorities();

        assertThat(sphinx.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void goneReturnsTargetCreatureYouDoNotControlToItsOwnersHand() {
        harness.addToBattlefield(player2, new SerraSphinx());
        Permanent sphinx = harness.addToBattlefieldAndReturn(player2, new SerraSphinx());

        harness.setHand(player1, List.of(new DeadGone()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castModalInstant(player1, 0, 1, List.of(sphinx.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Serra Sphinx");
        harness.assertOnBattlefield(player2, "Serra Sphinx");
    }

    @Test
    void goneRequiresThreeMana() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player2, new SerraSphinx());

        harness.setHand(player1, List.of(new DeadGone()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(sphinx.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void goneCannotTargetACreatureYouControl() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SerraSphinx());

        harness.setHand(player1, List.of(new DeadGone()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(sphinx.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
