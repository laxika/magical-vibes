package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PerennialBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Can play a land from the graveyard with Perennial Behemoth on the battlefield")
    void canPlayLandFromGraveyard() {
        harness.addToBattlefield(player1, new PerennialBehemoth());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Unearth returns Perennial Behemoth with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new PerennialBehemoth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent behemoth = findPermanent(player1, "Perennial Behemoth");
        assertThat(behemoth.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Perennial Behemoth");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Perennial Behemoth"));
    }
}
