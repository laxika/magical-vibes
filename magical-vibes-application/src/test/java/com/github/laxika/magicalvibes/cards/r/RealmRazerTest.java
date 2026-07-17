package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mountain"))
                .noneMatch(p -> p.getCard().getName().equals("Plains"));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mountain"))
                .anyMatch(c -> c.getName().equals("Plains"));

        // Realm Razer itself is not a land and stays on the battlefield.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Realm Razer"));
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
        harness.castInstant(player2, 0, razerId);
        harness.passBothPriorities(); // resolve Shock -> Realm Razer dies, lands return

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Realm Razer"));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mountain"));
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
        harness.castInstant(player2, 0, razerId);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isTrue();
    }
}
