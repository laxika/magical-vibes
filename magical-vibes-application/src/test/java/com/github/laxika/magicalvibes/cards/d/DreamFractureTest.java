package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamFracture.class, NettleSentinel.class})
class DreamFractureTest extends BaseCardTest {

    private void giveLibraries() {
        harness.setLibrary(player1, List.of(new NettleSentinel(), new NettleSentinel(), new NettleSentinel()));
        harness.setLibrary(player2, List.of(new NettleSentinel(), new NettleSentinel(), new NettleSentinel()));
    }

    @Test
    @DisplayName("Counters the spell; its controller draws a card and the caster draws a card")
    void countersSpellAndBothControllersDraw() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        giveLibraries();

        NettleSentinel sentinel = new NettleSentinel();
        harness.setHand(player1, List.of(sentinel));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new DreamFracture()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sentinel.getId());

        // Spell was countered — in its controller's graveyard, not on the battlefield.
        harness.assertNotOnBattlefield(player1, "Nettle Sentinel");
        harness.assertInGraveyard(player1, "Nettle Sentinel");

        // Its controller (player1) drew a card; the caster (player2) also drew a card.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Countering your own spell makes you draw twice (controller draw + caster draw)")
    void counteringOwnSpellDrawsTwice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        giveLibraries();

        NettleSentinel sentinel = new NettleSentinel();
        harness.setHand(player1, List.of(sentinel, new DreamFracture()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, sentinel.getId());

        harness.assertInGraveyard(player1, "Nettle Sentinel");
        // player1 is both the countered spell's controller and the caster → two draws.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not draw when the target spell leaves the stack before resolution")
    void doesNotDrawWhenTargetSpellLeavesStackBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        giveLibraries();

        NettleSentinel sentinel = new NettleSentinel();
        harness.setHand(player1, List.of(sentinel));
        harness.addMana(player1, ManaColor.GREEN, 1);

        DreamFracture fracture = new DreamFracture();
        harness.setHand(player2, List.of(fracture));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sentinel.getId());

        gd.stack.removeIf(entry -> entry.getCard().getId().equals(sentinel.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Dream Fracture");
    }
}
