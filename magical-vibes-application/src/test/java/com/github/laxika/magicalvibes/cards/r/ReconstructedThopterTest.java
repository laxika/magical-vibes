package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReconstructedThopterTest extends BaseCardTest {

    @Test
    @DisplayName("Unearth returns Reconstructed Thopter with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new ReconstructedThopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Reconstructed Thopter");
        assertThat(thopter.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Reconstructed Thopter");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Reconstructed Thopter");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Reconstructed Thopter"));
    }
}
