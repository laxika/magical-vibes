package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FangbladeBrigand.class, FangbladeEviscerator.class, GrizzlyBears.class})
class FangbladeBrigandTest extends BaseCardTest {

    @Test
    @DisplayName("The front face gets +1/+0 and first strike until end of turn")
    void frontFaceAbilityBoostsAndGrantsFirstStrike() {
        Permanent brigand = addReadyBrigand();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(brigand.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, brigand, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(brigand.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, brigand, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The back face pumps all creatures its controller controls")
    void backFaceAbilityBoostsOwnCreaturesOnly() {
        Permanent brigand = addReadyBrigand();
        transformToBack(brigand);
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(brigand.getPowerModifier()).isEqualTo(2);
        assertThat(ownBear.getPowerModifier()).isEqualTo(2);
        assertThat(opposingBear.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Day and night transform the two faces")
    void dayAndNightTransformTheFaces() {
        gd.dayNight = DayNight.DAY;
        Permanent brigand = addReadyBrigand();

        gd.spellsCastLastTurn.clear();
        advanceToUntap(player1);
        assertThat(brigand.getCard()).isInstanceOf(FangbladeEviscerator.class);
        assertThat(brigand.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUntap(player2);
        assertThat(brigand.getCard()).isInstanceOf(FangbladeBrigand.class);
        assertThat(brigand.isTransformed()).isFalse();
    }

    private Permanent addReadyBrigand() {
        return addCreatureReady(player1, new FangbladeBrigand());
    }

    private void transformToBack(Permanent brigand) {
        brigand.setCard(brigand.getOriginalCard().getBackFaceCard());
        brigand.setTransformed(true);
    }

    private void advanceToUntap(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
