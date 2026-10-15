package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GristTheHungerTide;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({SearchThePremises.class, GrizzlyBears.class, GristTheHungerTide.class})
class SearchThePremisesTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates when a creature attacks you")
    void investigatesWhenCreatureAttacksYou() {
        addSearchThePremises(player1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
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

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("Does not investigate when your creature attacks an opponent")
    void doesNotInvestigateWhenYourCreatureAttacksOpponent() {
        addSearchThePremises(player1);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Each attacker triggers separately when attacking both you and your planeswalker")
    void investigatesForMixedAttackTargets() {
        addSearchThePremises(player1);
        Permanent planeswalker = addPlaneswalker(player1);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1), Map.of(0, player1.getId(), 1, planeswalker.getId()));
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("The trigger still investigates after the attacking creature leaves")
    void investigatesAfterAttackerLeaves() {
        addSearchThePremises(player1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        gd.playerBattlefields.get(player2.getId()).remove(attacker);
        gd.playerGraveyards.get(player2.getId()).add(attacker.getCard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("The trigger still investigates after Search the Premises leaves")
    void investigatesAfterEnchantmentLeaves() {
        addSearchThePremises(player1);
        Permanent source = findPermanent(player1, "Search the Premises");
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("An investigated Clue can be sacrificed for two mana to draw a card")
    void investigatedClueDrawsCard() {
        addSearchThePremises(player1);
        addCreatureReady(player2, new GrizzlyBears());
        GrizzlyBears draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(draw));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(draw);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, java.util.UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private void addSearchThePremises(Player player) {
        harness.addToBattlefield(player, new SearchThePremises());
    }

    private Permanent addPlaneswalker(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GristTheHungerTide());
        permanent.setCounterCount(CounterType.LOYALTY, 3);
        return permanent;
    }
}
