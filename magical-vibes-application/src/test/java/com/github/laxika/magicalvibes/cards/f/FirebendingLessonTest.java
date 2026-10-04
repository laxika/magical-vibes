package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantKoi;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FirebendingLesson.class, GiantKoi.class})
class FirebendingLessonTest extends BaseCardTest {

    @Test
    void dealsTwoDamageWithoutKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantKoi());
        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void dealsFiveDamageWithKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantKoi());
        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Giant Koi");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickerRemainsOptionalWhenEnoughManaIsAvailable() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GiantKoi());
        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    void cannotKickWithoutPayingTheFullAdditionalCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantKoi());
        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Firebending Lesson");
    }

    @Test
    void kickedSpellDoesNotDealDamageWhenItsTargetLeavesTheBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantKoi());
        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Firebending Lesson");
    }
}
