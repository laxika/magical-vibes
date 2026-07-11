package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrimoireThiefTest extends BaseCardTest {

    // ===== Becomes-tapped trigger =====

    @Test
    @DisplayName("Becoming tapped exiles the top three cards of the opponent's library, tracked with it")
    void becomingTappedExilesTopThreeOfOpponentLibrary() {
        Permanent thief = addCreatureReady(player1, new GrimoireThief());
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock(), new Shock()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(thief.getId())).hasSize(3);
    }

    // ===== Sacrifice ability counters matching spells =====

    @Test
    @DisplayName("Sacrifice ability counters a spell whose name matches an exiled card")
    void sacrificeCountersMatchingSpell() {
        Permanent thief = addCreatureReady(player1, new GrimoireThief());
        harness.addMana(player1, ManaColor.BLUE, 1);
        gd.addToExile(player2.getId(), new Shock(), thief.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        // Player2 casts Shock at player1; player1 responds with the sacrifice ability.
        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        harness.assertLife(player1, 20); // Shock was countered, no damage
        harness.assertInGraveyard(player1, "Grimoire Thief");
    }

    @Test
    @DisplayName("Sacrifice ability does not counter a spell whose name is not among exiled cards")
    void sacrificeDoesNotCounterUnmatchedSpell() {
        Permanent thief = addCreatureReady(player1, new GrimoireThief());
        harness.addMana(player1, ManaColor.BLUE, 1);
        gd.addToExile(player2.getId(), new com.github.laxika.magicalvibes.cards.g.GrizzlyBears(), thief.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities(); // resolves the (empty) counter ability
        harness.passBothPriorities(); // resolves Shock

        harness.assertLife(player1, 18); // Shock resolved for 2 damage
    }
}
