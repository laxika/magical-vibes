package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.cards.s.ScorchingLava;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrimsTouch.class, RagingKavu.class, ScorchingLava.class})
class OrimsTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, prevents the next 2 damage to a target creature")
    void preventsTwoDamageWithoutKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RagingKavu());
        harness.setHand(player1, List.of(new OrimsTouch()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getDamagePreventionShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("With kicker, prevents the next 4 damage to a target player")
    void preventsFourDamageWithKicker() {
        harness.setHand(player1, List.of(new OrimsTouch()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(4);
    }

    @Test
    @DisplayName("Without kicker, prevents the next 2 damage to a target player")
    void preventsTwoDamageWithoutKickerToPlayer() {
        harness.setHand(player1, List.of(new OrimsTouch()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("With kicker, prevents the next 4 damage to a target creature")
    void preventsFourDamageWithKickerToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingKavu());
        harness.setHand(player1, List.of(new OrimsTouch()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(4);
    }

    @Test
    @DisplayName("The prevention shield prevents damage dealt to the target later this turn")
    void preventsDamageToTargetLaterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingKavu());
        harness.setHand(player1, List.of(new OrimsTouch()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.setHand(player1, List.of(new ScorchingLava()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Raging Kavu");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isZero();
    }
}
