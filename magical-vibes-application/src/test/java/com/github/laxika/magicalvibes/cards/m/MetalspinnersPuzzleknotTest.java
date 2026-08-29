package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
