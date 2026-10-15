package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilverbackShaman.class, GrizzlyBears.class, WrathOfGod.class, Unsummon.class})
class SilverbackShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Silverback Shaman dies from Wrath of God, draws a card")
    void diesFromWrathOfGodDrawsCard() {
        harness.addToBattlefield(player1, new SilverbackShaman());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Silverback Shaman");
        harness.assertInGraveyard(player1, "Silverback Shaman");
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Silverback Shaman"));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1 + 1);
    }

    @Test
    @DisplayName("Each Shaman dying simultaneously draws for its own controller")
    void simultaneousDeathsDrawForEachController() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.setHand(player2, List.of());
        SilverbackShaman firstDraw = new SilverbackShaman();
        SilverbackShaman secondDraw = new SilverbackShaman();
        GrizzlyBears opponentDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLibrary(player2, List.of(opponentDraw));
        harness.addToBattlefield(player1, new SilverbackShaman());
        harness.addToBattlefield(player1, new SilverbackShaman());
        harness.addToBattlefield(player2, new SilverbackShaman());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentDraw);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning Shaman to hand does not trigger a draw")
    void returningToHandDoesNotDraw() {
        SilverbackShaman shaman = new SilverbackShaman();
        harness.addToBattlefield(player1, shaman);
        harness.setHand(player1, List.of(new Unsummon()));
        SilverbackShaman libraryCard = new SilverbackShaman();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, findPermanent(player1, "Silverback Shaman").getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Silverback Shaman");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shaman);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Trample deals excess damage without drawing when Shaman survives")
    void tramplesOverBlockerWithoutDrawing() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        SilverbackShaman libraryCard = new SilverbackShaman();
        harness.setLibrary(player1, List.of(libraryCard));
        addCreatureReady(player1, new SilverbackShaman());
        var blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Silverback Shaman");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.stack).isEmpty();
    }
}
