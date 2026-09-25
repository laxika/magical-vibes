package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirstVolley.class, GnarledMass.class})
class FirstVolleyTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target creature and 1 damage to its controller")
    void dealsDamageToCreatureAndController() {
        harness.addToBattlefield(player2, new GnarledMass());
        harness.setHand(player1, List.of(new FirstVolley()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Gnarled Mass");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Gnarled Mass");
        assertThat(findPermanent(player2, "Gnarled Mass").getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to the controller even when the creature dies")
    void dealsDamageToControllerWhenCreatureDies() {
        harness.addToBattlefield(player2, new GnarledMass());
        harness.setHand(player1, List.of(new FirstVolley(), new FirstVolley(), new FirstVolley()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Gnarled Mass");
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Gnarled Mass");
        harness.assertInGraveyard(player2, "Gnarled Mass");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Fizzles when the target creature leaves the battlefield before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GnarledMass());
        harness.setHand(player1, List.of(new FirstVolley()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Gnarled Mass");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "First Volley");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target a creature its own controller controls")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new GnarledMass());
        harness.setHand(player1, List.of(new FirstVolley()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);

        UUID targetId = harness.getPermanentId(player1, "Gnarled Mass");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(findPermanent(player1, "Gnarled Mass").getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
    }
}
