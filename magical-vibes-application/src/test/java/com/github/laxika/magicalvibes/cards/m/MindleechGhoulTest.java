package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindleechGhoul.class, GrizzlyBears.class, Forest.class, Island.class})
class MindleechGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit leaves Mindleech Ghoul and the other creature on the battlefield")
    void decliningExploitDoesNothing() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest handCard = new Forest();
        harness.setHand(player2, List.of(handCard));

        castMindleechGhoul();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Mindleech Ghoul");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exploiting a creature makes each opponent exile a card from their hand")
    void exploitExilesFromOpponentsHands() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest exiledCard = new Forest();
        Island remainingCard = new Island();
        harness.setHand(player2, List.of(exiledCard, remainingCard));

        castMindleechGhoul();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player1, "Mindleech Ghoul");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiledCard);
    }

    @Test
    @DisplayName("Exploit still sacrifices a creature when opponents have empty hands")
    void exploitWithEmptyOpponentHand() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of());

        castMindleechGhoul();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mindleech Ghoul");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void castMindleechGhoul() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MindleechGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
