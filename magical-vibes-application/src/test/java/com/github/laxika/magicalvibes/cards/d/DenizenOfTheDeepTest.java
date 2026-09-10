package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.Archangel;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DenizenOfTheDeep.class, AirElemental.class, Archangel.class, Island.class})
class DenizenOfTheDeepTest extends BaseCardTest {

    @Test
    @DisplayName("ETB triggers when Denizen enters the battlefield")
    void etbTriggersOnEnter() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.castFromHand(player1, new DenizenOfTheDeep(), "{6}{U}{U}");

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Denizen of the Deep");

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Denizen of the Deep");
    }

    @Test
    @DisplayName("ETB returns all other creatures controlled by its controller to their owners' hands")
    void etbReturnsAllOtherCreatures() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new Archangel());
        harness.castFromHand(player1, new DenizenOfTheDeep(), "{6}{U}{U}");

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        // Only Denizen should remain on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1)
                .allMatch(p -> p.getCard().getName().equals("Denizen of the Deep"));

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Air Elemental", "Archangel");
    }

    @Test
    @DisplayName("ETB does not return opponent's creatures")
    void etbDoesNotReturnOpponentCreatures() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new Archangel());
        harness.castFromHand(player1, new DenizenOfTheDeep(), "{6}{U}{U}");

        resolveAllTriggers();

        GameData gd = harness.getGameData();

        // Opponent's Archangel should still be on battlefield
        harness.assertOnBattlefield(player2, "Archangel");

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Air Elemental");
    }

    @Test
    @DisplayName("ETB with no other creatures does nothing")
    void etbWithNoOtherCreaturesDoesNothing() {
        harness.castFromHand(player1, new DenizenOfTheDeep(), "{6}{U}{U}");

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        // Only Denizen on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1)
                .allMatch(p -> p.getCard().getName().equals("Denizen of the Deep"));

        // Hand should be empty
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB leaves noncreature permanents alone")
    void etbLeavesNoncreaturePermanentsAlone() {
        harness.addToBattlefield(player1, new Island());
        harness.castFromHand(player1, new DenizenOfTheDeep(), "{6}{U}{U}");

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Denizen of the Deep");
        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB returns a controlled creature to its owner's hand")
    void etbReturnsControlledCreatureToOwnerHand() {
        AirElemental ownedByOpponent = new AirElemental();
        ownedByOpponent.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, ownedByOpponent);
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new DenizenOfTheDeep(), "{6}{U}{U}");

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Air Elemental");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}

