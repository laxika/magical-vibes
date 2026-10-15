package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BywayCourier.class, WrathOfGod.class})
class BywayCourierTest extends BaseCardTest {

    @Test
    @DisplayName("When Byway Courier dies, its controller creates a Clue token")
    void deathTriggerCreatesClueTokenForController() {
        harness.addToBattlefield(player1, new BywayCourier());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, 0);

        harness.assertInGraveyard(player1, "Byway Courier");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        List<Permanent> clues = findPermanents(player1, "Clue");
        assertThat(clues).hasSize(1);
        Permanent clue = clues.getFirst();
        assertThat(clue.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(clue.getCard().getSubtypes()).contains(CardSubtype.CLUE);
        assertThat(clue.getCard().isToken()).isTrue();
        harness.assertNotOnBattlefield(player2, "Clue");
    }

    @Test
    @DisplayName("The created Clue is sacrificed as a cost and draws only on resolution")
    void clueSacrificeDrawsCardOnResolution() {
        harness.addToBattlefield(player1, new BywayCourier());
        harness.setLibrary(player1, List.of(new BywayCourier()));
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Byway Courier");
        harness.assertNotOnBattlefield(player2, "Clue");
    }

    @Test
    @DisplayName("Simultaneous deaths each create a Clue for the dying Courier's controller")
    void simultaneousDeathsInvestigateForEachController() {
        harness.addToBattlefield(player1, new BywayCourier());
        harness.addToBattlefield(player1, new BywayCourier());
        harness.addToBattlefield(player2, new BywayCourier());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(3);
        harness.assertNotOnBattlefield(player1, "Clue");
        harness.assertNotOnBattlefield(player2, "Clue");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Byway Courier");
        harness.assertNotOnBattlefield(player2, "Byway Courier");
    }
}
