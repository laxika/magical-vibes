package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TwinflameTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles damage from your spell to an opponent")
    void doublesSpellDamageToOpponent() {
        harness.addToBattlefield(player1, new TwinflameTyrant());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Doubles damage from your spell to an opponent's permanent")
    void doublesSpellDamageToOpponentsPermanent() {
        harness.addToBattlefield(player1, new TwinflameTyrant());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID serraId = harness.getPermanentId(player2, "Serra Angel");
        harness.castSorcery(player1, 0, 2, serraId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Does not double damage to you or your permanents")
    void doesNotDoubleDamageToControllerOrTheirPermanents() {
        harness.addToBattlefield(player1, new TwinflameTyrant());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.setLife(player1, 20);

        UUID serraId = harness.getPermanentId(player1, "Serra Angel");
        harness.castSorcery(player1, 0, 2, serraId);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Serra Angel");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Serra Angel"))
                .findFirst()
                .orElseThrow()
                .getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not double damage from a source controlled by an opponent")
    void doesNotDoubleOpponentsSourceDamage() {
        harness.addToBattlefield(player1, new TwinflameTyrant());
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Doubles your combat damage to an opponent")
    void doublesCombatDamageToOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new TwinflameTyrant());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
