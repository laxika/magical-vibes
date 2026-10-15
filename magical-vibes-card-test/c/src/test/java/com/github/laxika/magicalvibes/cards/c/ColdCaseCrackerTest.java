package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.m.Murder;
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

@CardUsed({ColdCaseCracker.class, WrathOfGod.class, Murder.class})
class ColdCaseCrackerTest extends BaseCardTest {

    @Test
    @DisplayName("When Cold Case Cracker dies, its controller investigates")
    void deathTriggerCreatesClueTokenForController() {
        harness.addToBattlefield(player1, new ColdCaseCracker());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cold Case Cracker");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        List<Permanent> clues = findPermanents(player1, "Clue");
        assertThat(clues).hasSize(1);
        Permanent clue = clues.getFirst();
        assertThat(clue.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(clue.getCard().getSubtypes()).contains(CardSubtype.CLUE);
        assertThat(clue.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("The Clue can be sacrificed for two mana to draw one card even while tapped")
    void clueCanBeSacrificedToDrawCard() {
        Permanent cracker = harness.addToBattlefieldAndReturn(player1, new ColdCaseCracker());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ColdCaseCracker(), new ColdCaseCracker()));
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castInstant(player2, 0, cracker.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.passBothPriorities();

        Permanent clue = findPermanent(player1, "Clue");
        clue.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Simultaneous deaths create one Clue per Cracker for its own controller")
    void simultaneousDeathsInvestigateForEachController() {
        harness.addToBattlefield(player1, new ColdCaseCracker());
        harness.addToBattlefield(player1, new ColdCaseCracker());
        harness.addToBattlefield(player2, new ColdCaseCracker());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(3);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }
}
