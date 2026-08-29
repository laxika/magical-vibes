package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScrapworkCohortTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 1/1 colorless Soldier artifact creature token")
    void etbCreatesSoldierToken() {
        harness.setHand(player1, List.of(new ScrapworkCohort()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Soldier"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
        assertThat(gqs.getEffectiveColors(gd, token)).isEmpty();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.isArtifact(gd, token)).isTrue();
        assertThat(gqs.isCreature(gd, token)).isTrue();
    }

    @Test
    @DisplayName("Unearth returns Scrapwork Cohort with haste and exiles it at the next end step")
    void unearthReturnsAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new ScrapworkCohort()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent cohort = findPermanent(player1, "Scrapwork Cohort");
        assertThat(cohort.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Scrapwork Cohort");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Scrapwork Cohort"));
    }
}
