package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NemesisOfReasonTest extends BaseCardTest {

    private void addAttacker() {
        Permanent nemesis = new Permanent(new NemesisOfReason());
        nemesis.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(nemesis);
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Attacking mills ten cards from the defending player's library")
    void attackingMillsTenFromDefender() {
        addAttacker();

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 20) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        declareAttack();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 10);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
    }

    @Test
    @DisplayName("Milled cards come off the top of the defender's library")
    void millsFromTopOfLibrary() {
        addAttacker();

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 12) {
            deck.removeFirst();
        }
        Card top = deck.get(0);
        Card eleventh = deck.get(10);

        declareAttack();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(top);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isEqualTo(eleventh);
    }

    @Test
    @DisplayName("Mills the whole library when fewer than ten cards remain")
    void millsEntireSmallLibrary() {
        addAttacker();

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 4) {
            deck.removeFirst();
        }

        declareAttack();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Only the defending player is milled, not the attacker's controller")
    void attackerControllerNotMilled() {
        addAttacker();

        int ownDeckBefore = gd.playerDecks.get(player1.getId()).size();

        declareAttack();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(ownDeckBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
