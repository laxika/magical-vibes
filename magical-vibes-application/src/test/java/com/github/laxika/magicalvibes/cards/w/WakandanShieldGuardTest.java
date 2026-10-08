package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakandanShieldGuard.class})
class WakandanShieldGuardTest extends BaseCardTest {

    @Test
    @DisplayName("When Wakandan Shield Guard enters, it creates a 1/1 white Soldier token")
    void etbCreatesSoldierToken() {
        harness.castFromHand(player1, new WakandanShieldGuard(), "{1}{W}");
        resolveAllTriggers();

        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().getPower()).isEqualTo(1);
        assertThat(soldier.getCard().getToughness()).isEqualTo(1);
        assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldier.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(soldier.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(soldier.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Entering without being cast creates exactly one untapped Soldier for its controller")
    void enteringWithoutCastingCreatesTokenForController() {
        harness.enterBattlefieldAndReturn(player2, new WakandanShieldGuard());

        assertThat(countPermanents(player2, "Soldier")).isZero();
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Soldier")).isEqualTo(1);
        assertThat(countPermanents(player1, "Soldier")).isZero();
        Permanent soldier = findPermanent(player2, "Soldier");
        assertThat(soldier.isTapped()).isFalse();
        assertThat(soldier.isAttacking()).isFalse();
        assertThat(soldier.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("The enters trigger creates a Soldier even after the Guard dies")
    void triggerResolvesAfterSourceDies() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WakandanShieldGuard());
        guard.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(guard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(guard.getCard());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        assertThat(countPermanents(player2, "Soldier")).isZero();
    }
}
