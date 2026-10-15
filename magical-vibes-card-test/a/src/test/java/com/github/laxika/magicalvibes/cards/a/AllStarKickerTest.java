package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AllStarKicker.class, GrizzlyBears.class})
class AllStarKickerTest extends BaseCardTest {

    @Test
    void castWithoutKickerDoesNotBuffTheTeam() {
        harness.setHand(player1, List.of(new AllStarKicker()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kicker = findPermanent(player1, "All-Star Kicker");
        assertThat(kicker.getEffectivePower()).isEqualTo(2);
        assertThat(kicker.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, kicker, Keyword.HASTE)).isFalse();
    }

    @Test
    void kickedCastBuffsAllOwnCreaturesAndGrantsHasteUntilEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AllStarKicker()));
        addBaseMana();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent kicker = findPermanent(player1, "All-Star Kicker");
        assertThat(kicker.getEffectivePower()).isEqualTo(3);
        assertThat(kicker.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, kicker, Keyword.HASTE)).isTrue();
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(kicker.getEffectivePower()).isEqualTo(2);
        assertThat(kicker.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, kicker, Keyword.HASTE)).isFalse();
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    void kickedAbilityAffectsOnlyOwnCreaturesPresentWhenItResolves() {
        Permanent opponentCreature = addCreatureReady(player2, new AllStarKicker());
        harness.setHand(player1, List.of(new AllStarKicker()));
        addBaseMana();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent beforeResolution = addCreatureReady(player1, new AllStarKicker());
        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new AllStarKicker());

        assertThat(beforeResolution.getEffectivePower()).isEqualTo(3);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.HASTE)).isTrue();
        assertThat(afterResolution.getEffectivePower()).isEqualTo(2);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.HASTE)).isFalse();
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

}
