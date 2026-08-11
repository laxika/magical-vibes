package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TamMindfulFirstYearTest extends BaseCardTest {

    @Test
    @DisplayName("Gives each other creature hexproof from its own colors")
    void givesEachOtherCreatureHexproofFromItsOwnColors() {
        Permanent tam = addCreatureReady(player1, new TamMindfulFirstYear());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasHexproofFromColor(gd, bears, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasHexproofFromColor(gd, bears, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasHexproofFromColor(gd, tam, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("The color-granting ability makes the target all colors and tracks the change")
    void colorGrantMakesTargetAllColorsAndTracksTheChange() {
        addCreatureReady(player1, new TamMindfulFirstYear());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, bears))
                .containsExactlyInAnyOrder(
                        CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
        assertThat(gqs.hasHexproofFromColor(gd, bears, CardColor.BLUE)).isTrue();

        gd.expireEndOfTurnFloatingEffects();
        bears.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasHexproofFromColor(gd, bears, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Rejects an opposing spell with one of the creature's colors")
    void rejectsOpposingSpellWithCreatureColor() {
        addCreatureReady(player1, new TamMindfulFirstYear());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new com.github.laxika.magicalvibes.cards.g.GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
