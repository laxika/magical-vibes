package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GhostlyPrisonTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent pays {2} for each creature attacking the controller")
    void opponentPaysTwoPerAttacker() {
        harness.addToBattlefield(player1, new GhostlyPrison());
        addReadyCreature(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(player2, List.of(0), null);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opponent cannot attack the controller without paying the tax")
    void opponentCannotAttackWithoutPayment() {
        harness.addToBattlefield(player1, new GhostlyPrison());
        addReadyCreature(player2);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Attacking the controller's planeswalker does not require the player-only tax")
    void planeswalkerIsNotTaxed() {
        harness.addToBattlefield(player1, new GhostlyPrison());
        Permanent planeswalker = addPlaneswalker(player1, 4);
        addReadyCreature(player2);

        declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId()));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
