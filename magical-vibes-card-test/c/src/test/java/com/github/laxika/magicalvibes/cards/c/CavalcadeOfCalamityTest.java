package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SenateCourier;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CavalcadeOfCalamity.class, SenateCourier.class})
class CavalcadeOfCalamityTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers for each attacking creature with power 1 or less")
    void triggersForSmallAttackingCreatures() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CavalcadeOfCalamity());
        addCreatureReady(player1, createCreature("One Power Creature", 1, 1));
        addCreatureReady(player1, createCreature("Zero Power Creature", 0, 1));

        declareAttackers(player1, List.of(1, 2));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not trigger for an attacking creature with power greater than 1")
    void doesNotTriggerForLargeAttacker() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CavalcadeOfCalamity());
        addCreatureReady(player1, createCreature("Two Power Creature", 2, 1));

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damages the planeswalker attacked by a small creature")
    void damagesAttackedPlaneswalker() {
        harness.addToBattlefield(player1, new CavalcadeOfCalamity());
        addCreatureReady(player1, createCreature("One Power Creature", 1, 1));
        Permanent planeswalker = addPlaneswalker(player2, 4);

        declareAttackers(player1, List.of(1), Map.of(1, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    private Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    @Test
    void powerIncreaseAfterAttackingDoesNotStopDamage() {
        harness.addToBattlefield(player1, new CavalcadeOfCalamity());
        Permanent attacker = addCreatureReady(player1, new SenateCourier());
        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).hasSize(1);

        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);

        harness.assertLife(player2, 19);
    }

    @Test
    void increasedPowerAtDeclarationPreventsTrigger() {
        harness.addToBattlefield(player1, new CavalcadeOfCalamity());
        Permanent attacker = addCreatureReady(player1, new SenateCourier());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackerLeavingDoesNotStopDamage() {
        harness.addToBattlefield(player1, new CavalcadeOfCalamity());
        Permanent attacker = addCreatureReady(player1, new SenateCourier());
        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);

        harness.assertLife(player2, 19);
    }

    @Test
    void opposingAttackerDoesNotTrigger() {
        harness.addToBattlefield(player1, new CavalcadeOfCalamity());
        addCreatureReady(player2, new SenateCourier());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }
}
