package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SearchThePremises.class, GrizzlyBears.class})
class SearchThePremisesTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates when a creature attacks you")
    void investigatesWhenCreatureAttacksYou() {
        addSearchThePremises(player1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0), null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Investigates when a creature attacks a planeswalker you control")
    void investigatesWhenCreatureAttacksYourPlaneswalker() {
        addSearchThePremises(player1);
        Permanent planeswalker = addPlaneswalker(player1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Investigates once for each attacking creature")
    void investigatesForEachAttackingCreature() {
        addSearchThePremises(player1);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("Does not investigate when your creature attacks an opponent")
    void doesNotInvestigateWhenYourCreatureAttacksOpponent() {
        addSearchThePremises(player1);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1), null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, java.util.UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private void addSearchThePremises(Player player) {
        gd.playerBattlefields.get(player.getId()).add(new Permanent(new SearchThePremises()));
    }

    private Permanent addPlaneswalker(Player player) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(4);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
