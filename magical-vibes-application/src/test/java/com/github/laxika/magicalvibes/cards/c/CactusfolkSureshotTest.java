package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeatherbackBaloth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CactusfolkSureshot.class, LeatherbackBaloth.class, GrizzlyBears.class})
class CactusfolkSureshotTest extends BaseCardTest {

    @Test
    void grantsTrampleAndHasteToOtherOwnCreaturesWithPowerAtLeastFour() {
        Permanent sureshot = addCreatureReady(player1, new CactusfolkSureshot());
        Permanent qualifyingCreature = addCreatureReady(player1, new LeatherbackBaloth());
        Permanent lowPowerCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new LeatherbackBaloth());

        advanceToCombat(player1);

        assertThat(gqs.hasKeyword(gd, sureshot, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, sureshot, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, qualifyingCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, qualifyingCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, lowPowerCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, lowPowerCreature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    void grantedKeywordsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new CactusfolkSureshot());
        Permanent qualifyingCreature = addCreatureReady(player1, new LeatherbackBaloth());

        advanceToCombat(player1);
        assertThat(gqs.hasKeyword(gd, qualifyingCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, qualifyingCreature, Keyword.HASTE)).isTrue();

        declareAttackers(List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, qualifyingCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, qualifyingCreature, Keyword.HASTE)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
