package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NemesisOfReason.class, JaceBeleren.class, InvasionOfZendikar.class})
class NemesisOfReasonTest extends BaseCardTest {

    private void addAttacker() {
        addCreatureReady(player1, new NemesisOfReason());
    }

    private void declareAttack() {
        declareAttackers(List.of(0));
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

    @Test
    @CardUsed(JaceBeleren.class)
    @DisplayName("Attacking a planeswalker mills its controller")
    void attackingPlaneswalkerMillsDefender() {
        Permanent planeswalker = declarePlaneswalkerAttack();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 10);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(planeswalker);
    }

    @Test
    @CardUsed(JaceBeleren.class)
    @DisplayName("The defending player still mills after the attacked planeswalker leaves")
    void millsAfterAttackedPlaneswalkerLeaves() {
        Permanent planeswalker = declarePlaneswalkerAttack();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerGraveyards.get(player2.getId()).add(planeswalker.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 10);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(11);
    }

    @Test
    @DisplayName("The attack trigger still mills after Nemesis leaves the battlefield")
    void millsAfterAttackerLeaves() {
        addAttacker();
        Permanent nemesis = gd.playerBattlefields.get(player1.getId()).getFirst();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        declareAttack();
        gd.playerBattlefields.get(player1.getId()).remove(nemesis);
        gd.playerGraveyards.get(player1.getId()).add(nemesis.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 10);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
    }

    @Test
    @DisplayName("Milling an empty library does not make the defending player lose")
    void emptyLibraryDoesNotCauseLoss() {
        addAttacker();
        harness.setLibrary(player2, List.of());
        declareAttack();

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @CardUsed(InvasionOfZendikar.class)
    @DisplayName("Attacking a battle mills its protector rather than its controller")
    void attackingBattleMillsProtector() {
        addAttacker();
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        battle.setProtectorPlayerId(player2.getId());
        int defenderDeckBefore = gd.playerDecks.get(player2.getId()).size();
        int controllerDeckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, battle.getId()));

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(defenderDeckBefore - 10);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(controllerDeckBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent declarePlaneswalkerAttack() {
        addAttacker();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
        return planeswalker;
    }
}
