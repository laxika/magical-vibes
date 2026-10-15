package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FearOfAbduction.class, FearOfLostTeeth.class, Murder.class})
class FearOfAbductionTest extends BaseCardTest {

    @Test
    @DisplayName("Additional cost exiles a creature you control and ETB exiles an opponent's creature")
    void additionalCostAndEnterTriggerExileCreatures() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new FearOfLostTeeth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FearOfLostTeeth());
        harness.setHand(player1, List.of(new FearOfAbduction()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false, sacrificed.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(sacrificed.getCard().getId());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent fear = findPermanent(player1, "Fear of Abduction");
        harness.assertNotOnBattlefield(player2, "Fear of Lost Teeth");
        assertThat(gd.getCardsExiledByPermanent(fear.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(sacrificed.getCard().getId(), target.getCard().getId());
    }

    @Test
    @DisplayName("Both exiled cards return to their owners' hands when Fear of Abduction leaves")
    void exiledTargetReturnsToOwnersHand() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new FearOfLostTeeth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FearOfLostTeeth());
        harness.setHand(player1, List.of(new FearOfAbduction()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false, sacrificed.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent fear = findPermanent(player1, "Fear of Abduction");
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, fear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fear of Abduction");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .contains(target.getCard().getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(sacrificed.getCard().getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ETB trigger cannot target a creature you control")
    void enterTriggerCannotTargetOwnCreature() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new FearOfLostTeeth());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new FearOfLostTeeth());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new FearOfLostTeeth());
        harness.setHand(player1, List.of(new FearOfAbduction()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false, sacrificed.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentTarget.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fear of Lost Teeth");
        harness.assertNotOnBattlefield(player2, "Fear of Lost Teeth");
    }

    @Test
    @DisplayName("Leaving before the enter trigger resolves returns the cost card but strands the target in exile")
    void leavesBeforeEnterTriggerResolves() {
        Permanent costCreature = harness.addToBattlefieldAndReturn(player1, new FearOfLostTeeth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FearOfLostTeeth());
        harness.setHand(player1, List.of(new FearOfAbduction()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false, costCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        Permanent fear = findPermanent(player1, "Fear of Abduction");
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, fear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(costCreature.getCard().getId());
        harness.assertOnBattlefield(player2, "Fear of Lost Teeth");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fear of Lost Teeth");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(target.getCard().getId());
        harness.assertNotInHand(player2, "Fear of Lost Teeth");
    }

    @Test
    @DisplayName("Casting requires a creature controlled by the caster")
    void cannotExileOpponentsCreatureAsCost() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FearOfLostTeeth());
        harness.setHand(player1, List.of(new FearOfAbduction()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(), false, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Fear of Lost Teeth");
        harness.assertInHand(player1, "Fear of Abduction");
    }
}
