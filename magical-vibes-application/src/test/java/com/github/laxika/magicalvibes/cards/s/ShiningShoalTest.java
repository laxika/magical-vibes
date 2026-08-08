package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShiningShoalTest extends BaseCardTest {

    @Test
    @DisplayName("Cast for X=3 shields the next 3 damage from the chosen source")
    void castForXCreatesShieldOfSizeX() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShiningShoal()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, 3, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bear.getId());

        assertThat(gd.sourceDamageRedirectShields).hasSize(1);
        assertThat(gd.sourceDamageRedirectShields.getFirst().protectedPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.sourceDamageRedirectShields.getFirst().damageSourceId()).isEqualTo(bear.getId());
        assertThat(gd.sourceDamageRedirectShields.getFirst().remainingAmount()).isEqualTo(3);
        assertThat(gd.sourceDamageRedirectShields.getFirst().redirectTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The chosen source's combat damage is dealt to the target instead")
    void redirectsCombatDamageToTargetPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player2, new AirElemental());

        harness.setHand(player1, List.of(new ShiningShoal()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castInstant(player1, 0, 3, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // 3 of the 4 damage is redirected to player2; player1 takes the remaining 1.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Redirected damage can be dealt to a creature target")
    void redirectsCombatDamageToTargetCreature() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new ShiningShoal()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0, 2, victim.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(victim.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiling a white card with mana value X pays the alternative cost")
    void alternativeCostExilesWhiteCardWithManaValueX() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShiningShoal(), new ShiningShoal()));

        // Shining Shoal's own mana value is 2, so exiling it pays for X = 2 with no mana spent.
        harness.castInstantWithAlternateExileFromHand(player1, 0, 2, player2.getId(), 1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bear.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.sourceDamageRedirectShields).hasSize(1);
        assertThat(gd.sourceDamageRedirectShields.getFirst().remainingAmount()).isEqualTo(2);
    }

    @Test
    @DisplayName("The exiled card's mana value must equal the chosen X")
    void alternativeCostRejectsMismatchedManaValue() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShiningShoal(), new ShiningShoal()));

        assertThatThrownBy(() ->
                harness.castInstantWithAlternateExileFromHand(player1, 0, 3, player2.getId(), 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage from a source other than the chosen one is unaffected")
    void doesNotAffectOtherSources() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new ShiningShoal()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        harness.forceActivePlayer(player2);
        other.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.sourceDamageRedirectShields).hasSize(1);
    }
}
