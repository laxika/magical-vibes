package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AkkiRaider;
import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.cards.f.FrostOgre;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShiningShoal.class, AkkiRaider.class, FirstVolley.class, FrostOgre.class, GnarledMass.class})
class ShiningShoalTest extends BaseCardTest {

    @Test
    @DisplayName("Cast for X=3 shields the next 3 damage from the chosen source")
    void castForXCreatesShieldOfSizeX() {
        Permanent source = addCreatureReady(player2, new GnarledMass());
        harness.setHand(player1, List.of(new ShiningShoal()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, 3, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.sourceDamageRedirectShields).hasSize(1);
        assertThat(gd.sourceDamageRedirectShields.getFirst().protectedPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.sourceDamageRedirectShields.getFirst().damageSourceId()).isEqualTo(source.getId());
        assertThat(gd.sourceDamageRedirectShields.getFirst().remainingAmount()).isEqualTo(3);
        assertThat(gd.sourceDamageRedirectShields.getFirst().redirectTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The chosen source's combat damage is dealt to the target instead")
    void redirectsCombatDamageToTargetPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player2, new FrostOgre());

        harness.setHand(player1, List.of(new ShiningShoal()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castInstant(player1, 0, 3, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(true);
        resolveCombat(player2);

        // 3 of the 5 damage is redirected to player2; player1 takes the remaining 2.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Redirected damage can be dealt to a creature target")
    void redirectsCombatDamageToTargetCreature() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, new AkkiRaider());
        Permanent victim = addCreatureReady(player2, new GnarledMass());

        harness.setHand(player1, List.of(new ShiningShoal()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0, 2, victim.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(victim.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiling a white card with mana value X pays the alternative cost")
    void alternativeCostExilesWhiteCardWithManaValueX() {
        Permanent source = addCreatureReady(player2, new GnarledMass());
        harness.setHand(player1, List.of(new ShiningShoal(), new ShiningShoal()));

        // Shining Shoal's own mana value is 2, so exiling it pays for X = 2 with no mana spent.
        harness.castInstantWithAlternateExileFromHand(player1, 0, 2, player2.getId(), 1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.sourceDamageRedirectShields).hasSize(1);
        assertThat(gd.sourceDamageRedirectShields.getFirst().remainingAmount()).isEqualTo(2);
    }

    @Test
    @DisplayName("The exiled card's mana value must equal the chosen X")
    void alternativeCostRejectsMismatchedManaValue() {
        addCreatureReady(player2, new GnarledMass());
        harness.setHand(player1, List.of(new ShiningShoal(), new ShiningShoal()));

        assertThatThrownBy(() ->
                harness.castInstantWithAlternateExileFromHand(player1, 0, 3, player2.getId(), 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The alternative cost rejects a nonwhite card with the right mana value")
    void alternativeCostRejectsNonWhiteCardWithMatchingManaValue() {
        addCreatureReady(player2, new GnarledMass());
        harness.setHand(player1, List.of(new ShiningShoal(), new AkkiRaider()));

        assertThatThrownBy(() ->
                harness.castInstantWithAlternateExileFromHand(player1, 0, 2, player2.getId(), 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A zero-X cast resolves without creating a damage redirect shield")
    void zeroXCreatesNoShield() {
        addCreatureReady(player2, new GnarledMass());
        harness.setHand(player1, List.of(new ShiningShoal()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.sourceDamageRedirectShields).isEmpty();
    }

    @Test
    @DisplayName("Damage to a creature you control is redirected")
    void redirectsDamageToControlledCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player2, new GnarledMass());
        Permanent blocker = addCreatureReady(player1, new GnarledMass());

        harness.setHand(player1, List.of(new ShiningShoal()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castInstant(player1, 0, 3, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A damage spell on the stack is a legal source choice")
    void allowsDamageSpellOnStackAsSourceChoice() {
        Permanent victim = addCreatureReady(player1, new GnarledMass());
        FirstVolley volley = new FirstVolley();
        harness.setHand(player2, List.of(volley));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, victim.getId());

        harness.setHand(player1, List.of(new ShiningShoal()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, 1, player1.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(volley.getId());
    }

    @Test
    @DisplayName("The redirect shield expires at end of turn")
    void shieldExpiresAtEndOfTurn() {
        Permanent source = addCreatureReady(player2, new GnarledMass());
        harness.setHand(player1, List.of(new ShiningShoal()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());
        assertThat(gd.sourceDamageRedirectShields).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.sourceDamageRedirectShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from a source other than the chosen one is unaffected")
    void doesNotAffectOtherSources() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent chosen = addCreatureReady(player2, new GnarledMass());
        Permanent other = addCreatureReady(player2, new GnarledMass());

        harness.setHand(player1, List.of(new ShiningShoal()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.sourceDamageRedirectShields).hasSize(1);
    }
}
