package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvenTrooper;
import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EqualTreatment.class, AvenTrooper.class, Blaze.class, SerraAngel.class})
class EqualTreatmentTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card")
    void drawsACard() {
        harness.setLibrary(player1, List.of(new AvenTrooper()));

        harness.castFromHand(player1, new EqualTreatment(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Aven Trooper");
    }

    @Test
    @DisplayName("Replaces one damage to a player with two")
    void replacesDamageToPlayer() {
        castEqualTreatment();
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Replaces larger damage to a player with two")
    void replacesLargerDamageToPlayerWithTwo() {
        castEqualTreatment();
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 3, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Replaces damage to a permanent with two")
    void replacesDamageToPermanent() {
        castEqualTreatment();
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 1, angel.getId());
        harness.passBothPriorities();

        assertThat(angel.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Replaces combat damage with two")
    void replacesCombatDamage() {
        castEqualTreatment();
        AvenTrooper attacker = new AvenTrooper();
        addCreatureReady(player2, attacker);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        declareAttackers(player2, List.of(0));

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Stops replacing damage after the turn ends")
    void replacementExpiresAtEndOfTurn() {
        castEqualTreatment();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.setLife(player1, 20);

        harness.castSorcery(player2, 0, 1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    private void castEqualTreatment() {
        harness.castFromHand(player1, new EqualTreatment(), "{1}{W}");
        harness.passBothPriorities();
    }
}
