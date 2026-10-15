package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MetalspinnersPuzzleknot.class, Forest.class})
class MetalspinnersPuzzleknotTest extends BaseCardTest {

    @Test
    void enteringBattlefieldDrawsACardAndLosesLife() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new MetalspinnersPuzzleknot()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void sacrificeAbilityDrawsACardAndLosesLife() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent puzzleknot = harness.addToBattlefieldAndReturn(player1, new MetalspinnersPuzzleknot());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int puzzleknotIndex = gd.playerBattlefields.get(player1.getId()).indexOf(puzzleknot);
        harness.activateAbility(player1, puzzleknotIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(puzzleknot);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(puzzleknot.getCard());
    }

    @Test
    void sacrificingInResponseToEnterTriggerStillResolvesBothAbilities() {
        Forest firstCard = new Forest();
        Forest secondCard = new Forest();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(new MetalspinnersPuzzleknot()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Metalspinner's Puzzleknot");
        harness.assertInGraveyard(player1, "Metalspinner's Puzzleknot");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard);

        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
    }

    @Test
    void tappedPuzzleknotCanBeSacrificedByNonactivePlayer() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setHand(player2, List.of());
        Permanent puzzleknot = harness.addToBattlefieldAndReturn(player2, new MetalspinnersPuzzleknot());
        puzzleknot.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.assertInGraveyard(player2, "Metalspinner's Puzzleknot");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }
}
