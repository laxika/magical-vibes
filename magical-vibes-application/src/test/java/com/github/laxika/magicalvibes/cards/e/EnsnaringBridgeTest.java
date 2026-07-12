package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnsnaringBridgeTest extends BaseCardTest {

    private Permanent addCreature(Player controller, int power) {
        Card creature = new Card();
        creature.setName("Test Creature");
        creature.setType(CardType.CREATURE);
        creature.setSubtypes(new ArrayList<>());
        creature.setPower(power);
        creature.setToughness(power);
        Permanent perm = new Permanent(creature);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(controller.getId()).add(perm);
        return perm;
    }

    private void setHandSize(Player player, int count) {
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Card c = new Card();
            c.setName("Hand Card " + i);
            hand.add(c);
        }
        gd.playerHands.put(player.getId(), hand);
    }

    @Test
    @DisplayName("Creature with power greater than controller's hand size cannot attack")
    void higherPowerCannotAttack() {
        harness.addToBattlefield(player1, new EnsnaringBridge());
        setHandSize(player1, 2);
        Permanent attacker = addCreature(player1, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creature with power equal to controller's hand size can attack")
    void equalPowerCanAttack() {
        harness.addToBattlefield(player1, new EnsnaringBridge());
        setHandSize(player1, 2);
        Permanent attacker = addCreature(player1, 2);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareAttackers(gd, player1, List.of(index));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Restriction uses the Bridge controller's hand size, not the attacker's controller")
    void restrictionUsesBridgeControllerHand() {
        // player1 controls the Bridge with an empty hand; player2 has a full hand but attacks.
        harness.addToBattlefield(player1, new EnsnaringBridge());
        setHandSize(player1, 0);
        setHandSize(player2, 7);
        Permanent attacker = addCreature(player2, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restriction lifts once the controller's hand grows large enough")
    void largerHandLiftsRestriction() {
        harness.addToBattlefield(player1, new EnsnaringBridge());
        setHandSize(player1, 3);
        Permanent attacker = addCreature(player1, 3);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareAttackers(gd, player1, List.of(index));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
