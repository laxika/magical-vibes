package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({CinderStorm.class, SerraAngel.class, Counterspell.class})
class CinderStormTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 7 damage to target player")
    void dealsSevenDamageToPlayer() {
        harness.setHand(player1, List.of(new CinderStorm()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("Deals 7 damage to a target creature, killing a 4/4")
    void killsFourToughnessCreature() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new CinderStorm()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Can deal 7 damage to its controller")
    void canDamageItsController() {
        harness.setHand(player1, List.of(new CinderStorm()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Cinder Storm");
    }

    @Test
    @DisplayName("Can target a creature its controller controls")
    void canDamageOwnCreature() {
        harness.addToBattlefield(player1, new SerraAngel());
        harness.setHand(player1, List.of(new CinderStorm()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Serra Angel"));

        harness.assertNotOnBattlefield(player1, "Serra Angel");
        harness.assertInGraveyard(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Deals no damage when countered")
    void dealsNoDamageWhenCountered() {
        harness.setHand(player1, List.of(new CinderStorm()));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, gd.stack.getFirst().getCard().getId());

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Cinder Storm");
        harness.assertInGraveyard(player2, "Counterspell");
    }
}
