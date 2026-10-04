package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LeafGilder;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Faultgrinder.class, Mountain.class, LeafGilder.class})
class FaultgrinderTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: ETB destroys target land and Faultgrinder stays")
    void hardcastDestroysLandAndStays() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new Faultgrinder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Mountain");
        harness.assertOnBattlefield(player1, "Faultgrinder");
    }

    @Test
    @DisplayName("Evoke: paying {4}{R}, ETB destroys the land and Faultgrinder is sacrificed")
    void evokeDestroysAndSacrificesSelf() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new Faultgrinder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castCreatureWithEvoke(player1, 0, targetId);
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Mountain");
        harness.assertNotOnBattlefield(player1, "Faultgrinder");
        harness.assertInGraveyard(player1, "Faultgrinder");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a non-land creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new LeafGilder());
        harness.setHand(player1, List.of(new Faultgrinder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        UUID creatureId = harness.getPermanentId(player2, "Leaf Gilder");
        assertThatThrownBy(() ->
                harness.castCreature(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The entry ability can destroy its controller's land")
    void destroysOwnLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new Faultgrinder()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castCreature(player1, 0, harness.getPermanentId(player1, "Mountain"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Faultgrinder");
    }

    @Test
    @DisplayName("Evoke still sacrifices Faultgrinder when the land target leaves")
    void evokeSacrificesEvenWhenLandTargetLeaves() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new Faultgrinder()));
        harness.addMana(player1, ManaColor.RED, 5);
        UUID targetId = harness.getPermanentId(player2, "Mountain");

        harness.castCreatureWithEvoke(player1, 0, targetId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Faultgrinder");
        assertThat(gd.stack).hasSize(2);

        var land = gd.playerBattlefields.get(player2.getId()).getFirst();
        gd.playerBattlefields.get(player2.getId()).remove(land);
        gd.playerHands.get(player2.getId()).add(land.getCard());
        resolveAllTriggers();

        harness.assertInHand(player2, "Mountain");
        harness.assertNotInGraveyard(player2, "Mountain");
        harness.assertInGraveyard(player1, "Faultgrinder");
        harness.assertNotOnBattlefield(player1, "Faultgrinder");
    }

    @Test
    @DisplayName("Evoke cannot be cast with less than five mana")
    void evokeRequiresFullCost() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new Faultgrinder()));
        harness.addMana(player1, ManaColor.RED, 4);
        UUID targetId = harness.getPermanentId(player2, "Mountain");

        assertThatThrownBy(() -> harness.castCreatureWithEvoke(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Faultgrinder");
        harness.assertNotOnBattlefield(player1, "Faultgrinder");
    }
}
