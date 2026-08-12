package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnstableFootingTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, it can be cast without a target and prevents damage this turn")
    void withoutKickerHasNoTargetAndPreventsDamage() {
        gd.playerDamagePreventionShields.put(player2.getId(), 10);
        harness.setHand(player1, List.of(new UnstableFooting()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("When kicked, it deals 5 damage to a target player despite prevention")
    void kickedDealsFiveDamageDespitePrevention() {
        gd.playerDamagePreventionShields.put(player2.getId(), 10);
        harness.setHand(player1, List.of(new UnstableFooting()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("A kicked spell must target a player or planeswalker")
    void kickedCannotTargetCreature() {
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnstableFooting()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player");
    }

    @Test
    @DisplayName("The damage prevention lock wears off at end of turn")
    void preventionLockClearedAtEndOfTurn() {
        gd.damageCantBePreventedThisTurn = true;

        new com.github.laxika.magicalvibes.service.turn.TurnCleanupService(null, null)
                .resetEndOfTurnModifiers(gd);

        assertThat(gd.damageCantBePreventedThisTurn).isFalse();
    }
}
