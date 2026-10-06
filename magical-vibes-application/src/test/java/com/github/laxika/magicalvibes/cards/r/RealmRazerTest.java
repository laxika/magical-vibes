package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RealmRazer.class, Forest.class, Mountain.class, Plains.class, Shock.class})
class RealmRazerTest extends BaseCardTest {

    /** Casts Realm Razer and resolves its ETB "exile all lands" trigger. */
    private void castAndResolveRealmRazer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RealmRazer()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> enters, ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB -> exile all lands
    }

    @Test
    @DisplayName("ETB exiles every land on the battlefield, both players")
    void etbExilesAllLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Plains());

        castAndResolveRealmRazer();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertNotOnBattlefield(player2, "Plains");

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mountain"))
                .anyMatch(c -> c.getName().equals("Plains"));

        // Realm Razer itself is not a land and stays on the battlefield.
        harness.assertOnBattlefield(player1, "Realm Razer");
    }

    @Test
    @DisplayName("Exiled lands return under their owners' control when Realm Razer dies")
    void landsReturnUnderOwnersControlOnLeave() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        castAndResolveRealmRazer();

        // Kill Realm Razer with Shock (4/2).
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID razerId = harness.getPermanentId(player1, "Realm Razer");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, razerId);

        harness.assertNotOnBattlefield(player1, "Realm Razer");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Mountain");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities(); // resolve the leaves-the-battlefield trigger

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returned lands enter the battlefield tapped")
    void returnedLandsEnterTapped() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        assertThat(forest.isTapped()).isFalse();

        castAndResolveRealmRazer();

        // Kill Realm Razer.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID razerId = harness.getPermanentId(player1, "Realm Razer");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, razerId);
        harness.passBothPriorities(); // resolve the leaves-the-battlefield trigger

        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing Realm Razer before its enter trigger resolves exiles lands permanently")
    void removalBeforeEnterTriggerResolvesLeavesLandsExiled() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RealmRazer()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Realm Razer"));

        harness.assertNotOnBattlefield(player1, "Realm Razer");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities(); // the leave trigger has no cards to return
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
        harness.passBothPriorities(); // the enter trigger still exiles all lands

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Mountain");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mountain"));
        assertThat(gd.stack).isEmpty();
    }
}
