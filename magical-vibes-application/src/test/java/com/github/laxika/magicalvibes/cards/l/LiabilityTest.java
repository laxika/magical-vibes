package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.castInstant(player2, 0, mindStoneId);
        harness.passBothPriorities();
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
        harness.castInstant(player1, 0, mindStoneId);
        harness.passBothPriorities();
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
        harness.castInstant(player1, 0, tokenId);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Token Creature"));
    }
}
