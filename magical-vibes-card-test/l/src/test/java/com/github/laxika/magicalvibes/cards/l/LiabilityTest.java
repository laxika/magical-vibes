package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.t.Tranquility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({Liability.class, MindStone.class, Naturalize.class, DoomBlade.class, IvoryMask.class,
        Tranquility.class})
class LiabilityTest extends BaseCardTest {

    @Test
    @DisplayName("The owner of a nontoken permanent put into a graveyard loses 1 life")
    void graveyardOwnerLosesLife() {
        harness.addToBattlefield(player1, new Liability());
        harness.addToBattlefield(player1, new MindStone());
        UUID mindStoneId = harness.getPermanentId(player1, "Mind Stone");
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, mindStoneId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Liability also triggers for an opponent's graveyard")
    void opponentGraveyardOwnerLosesLife() {
        harness.addToBattlefield(player1, new Liability());
        harness.addToBattlefield(player2, new MindStone());
        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, mindStoneId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mind Stone");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A token permanent does not trigger Liability")
    void tokenDoesNotTrigger() {
        harness.addToBattlefield(player1, new Liability());
        Card token = new Card();
        token.setName("Token Creature");
        token.setType(CardType.CREATURE);
        token.setManaCost("");
        token.setToken(true);
        token.setColor(CardColor.GREEN);
        token.setPower(2);
        token.setToughness(2);
        harness.addToBattlefield(player2, token);
        UUID tokenId = harness.getPermanentId(player2, "Token Creature");
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, tokenId);

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Token Creature");
    }

    @Test
    @DisplayName("Liability triggers when it is itself put into a graveyard")
    void ownDestructionLosesLife() {
        harness.addToBattlefield(player1, new Liability());
        UUID liabilityId = harness.getPermanentId(player1, "Liability");
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, liabilityId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Liability");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A permanent's owner loses life even when its controller is another player")
    void stolenPermanentOwnerLosesLife() {
        harness.addToBattlefield(player1, new Liability());
        MindStone mindStone = new MindStone();
        mindStone.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, mindStone);
        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, mindStoneId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Both Liabilities see every enchantment destroyed simultaneously")
    void simultaneousDestructionTriggersForEachPermanent() {
        harness.addToBattlefield(player1, new Liability());
        harness.addToBattlefield(player2, new Liability());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Tranquility()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player1, "Liability");
        harness.assertInGraveyard(player2, "Liability");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Shroud does not prevent Liability's non-targeting life loss")
    void shroudDoesNotPreventLifeLoss() {
        harness.addToBattlefield(player1, new Liability());
        harness.addToBattlefield(player2, new IvoryMask());
        harness.addToBattlefield(player2, new MindStone());
        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, mindStoneId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mind Stone");
        harness.assertLife(player2, 19);
    }
}
