package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrailOfEvidence.class, Shock.class, Divination.class, GrizzlyBears.class})
class TrailOfEvidenceTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates when you cast an instant")
    void investigatesForInstant() {
        harness.addToBattlefield(player1, new TrailOfEvidence());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Investigates when you cast a sorcery")
    void investigatesForSorcery() {
        harness.addToBattlefield(player1, new TrailOfEvidence());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Does not investigate for a creature spell")
    void doesNotInvestigateForCreature() {
        harness.addToBattlefield(player1, new TrailOfEvidence());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void doesNotInvestigateForOpponentsSpell() {
        harness.addToBattlefield(player1, new TrailOfEvidence());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        harness.assertLife(player1, 18);
    }

    @Test
    void investigatesForEachSpellDuringOpponentsTurnBeforeTheSpellResolves() {
        harness.addToBattlefield(player1, new TrailOfEvidence());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        harness.assertLife(player2, 16);
    }

    @Test
    void tappedClueIsSacrificedForTwoManaAndDrawsOnlyOnResolution() {
        harness.addToBattlefield(player1, new TrailOfEvidence());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Shock()));
        TrailOfEvidence drawnCard = new TrailOfEvidence();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        Permanent clue = findPermanent(player1, "Clue");
        clue.tap();
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }
}
