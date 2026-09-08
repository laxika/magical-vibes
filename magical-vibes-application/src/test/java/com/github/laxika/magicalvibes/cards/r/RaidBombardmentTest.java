package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RaidBombardmentTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers for each attacking creature with power 2 or less")
    void triggersForSmallAttackingCreatures() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RaidBombardment());
        addCreatureReady(player1, new GoblinPiker());
        addCreatureReady(player1, createCreature("Zero Power Creature", 0, 1));

        declareAttackers(player1, List.of(1, 2), null);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not trigger for an attacking creature with power greater than 2")
    void doesNotTriggerForLargeAttacker() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RaidBombardment());
        addCreatureReady(player1, new AirElemental());

        declareAttackers(player1, List.of(1), null);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damages the planeswalker attacked by a small creature")
    void damagesAttackedPlaneswalker() {
        harness.addToBattlefield(player1, new RaidBombardment());
        addCreatureReady(player1, createCreature("Zero Power Creature", 0, 1));
        Permanent planeswalker = addPlaneswalker(player2, 4);

        declareAttackers(player1, List.of(1), Map.of(1, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    private Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
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
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
