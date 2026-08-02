package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SanctifiedChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts own creatures and grants first strike to own white creatures")
    void boostsOwnCreaturesAndGrantsFirstStrikeToWhiteCreatures() {
        Permanent whiteCreature = addCreatureReady(player1, new EliteVanguard());
        Permanent nonWhiteCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SanctifiedCharge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, whiteCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, whiteCreature, Keyword.FIRST_STRIKE)).isTrue();

        assertThat(gqs.getEffectivePower(gd, nonWhiteCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nonWhiteCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, nonWhiteCreature, Keyword.FIRST_STRIKE)).isFalse();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The boost and first strike grant wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent whiteCreature = addCreatureReady(player1, new EliteVanguard());

        harness.setHand(player1, List.of(new SanctifiedCharge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, whiteCreature, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, whiteCreature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, whiteCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

}
