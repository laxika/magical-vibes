package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IsengardUnleashed.class, Shock.class, SerraAngel.class, GrizzlyBears.class})
class IsengardUnleashedTest extends BaseCardTest {

    @Test
    @DisplayName("Triples your damage to opponents and their permanents, but not your own")
    void triplesDamageOnlyToOpponentsAndTheirPermanents() {
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(
                new IsengardUnleashed(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 8);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
        assertThat(gqs.isDamagePreventable(gd)).isFalse();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Serra Angel"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Serra Angel");

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Serra Angel"));
        harness.passBothPriorities();
        Permanent ownAngel = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Serra Angel"))
                .findFirst()
                .orElseThrow();
        assertThat(ownAngel.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not triple damage from an opponent's source")
    void doesNotTripleOpponentsSourceDamage() {
        harness.setHand(player1, List.of(new IsengardUnleashed()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Triples your combat damage to an opponent")
    void triplesCombatDamageToOpponent() {
        harness.setHand(player1, List.of(new IsengardUnleashed()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GrizzlyBears());

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Flashback applies the effect and exiles Isengard Unleashed")
    void flashbackAppliesEffectAndExilesCard() {
        harness.setGraveyard(player1, List.of(new IsengardUnleashed()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 8);
        harness.setLife(player2, 20);

        harness.castFlashback(player1, 0, List.of());
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Isengard Unleashed"));
    }
}
