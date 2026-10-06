package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Staggershock;
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

@CardUsed({ReinforcedBulwark.class, Staggershock.class})
class ReinforcedBulwarkTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 1 damage dealt to controller")
    void preventsOneOfLargerHit() {
        harness.setLife(player1, 20);
        Permanent bulwark = addCreatureReady(player1, new ReinforcedBulwark());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(bulwark.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Staggershock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Shield wears off at end of turn")
    void shieldWearsOffAtEndOfTurn() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new ReinforcedBulwark());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Staggershock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void shieldIsConsumedByFirstDamageEvent() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new ReinforcedBulwark());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Staggershock(), new Staggershock()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 19);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 17);
    }

    @Test
    void shieldsFromTwoBulwarksAccumulate() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new ReinforcedBulwark());
        addCreatureReady(player1, new ReinforcedBulwark());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Staggershock(), new Staggershock()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 20);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);
    }

    @Test
    void shieldDoesNotProtectOpponentOrGetConsumedByTheirDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ReinforcedBulwark());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Staggershock(), new Staggershock()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.castAndResolveInstant(player2, 0, player2.getId());
        harness.assertLife(player2, 18);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 19);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ReinforcedBulwark());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent bulwark = addCreatureReady(player1, new ReinforcedBulwark());
        bulwark.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }
}
