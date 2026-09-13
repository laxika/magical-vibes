package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvatarOfFury.class, RhysticCave.class})
class AvatarOfFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast Avatar of Fury for {R}{R} without cost reduction")
    void cannotCastWithoutReduction() {
        assertThatThrownBy(() -> harness.castFromHand(player1, new AvatarOfFury(), "{R}{R}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast Avatar of Fury for {R}{R} when an opponent controls seven lands")
    void canCastWithSevenOpponentLands() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player2, new RhysticCave());
        }

        harness.castFromHand(player1, new AvatarOfFury(), "{R}{R}");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The cost reduction does not apply with only six opposing lands")
    void doesNotReduceWithSixOpponentLands() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new RhysticCave());
        }

        assertThatThrownBy(() -> harness.castFromHand(player1, new AvatarOfFury(), "{R}{R}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The cost reduction does not apply to seven lands you control")
    void doesNotReduceWithSevenOwnLands() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new RhysticCave());
        }

        assertThatThrownBy(() -> harness.castFromHand(player1, new AvatarOfFury(), "{R}{R}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Avatar of Fury gets +1/+0 for {R} until end of turn")
    void boostsItselfUntilEndOfTurn() {
        Permanent avatar = addCreatureReady(player1, new AvatarOfFury());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(6);
    }
}
