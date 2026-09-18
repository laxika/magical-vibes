package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnsweredPrayers.class, GrizzlyBears.class})
class AnsweredPrayersTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life and becomes a 3/3 Angel with flying when a creature enters")
    void gainsLifeAndBecomesAngelWhenCreatureEnters() {
        harness.setLife(player1, 20);
        Permanent prayers = harness.addToBattlefieldAndReturn(player1, new AnsweredPrayers());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gqs.isCreature(gd, prayers)).isTrue();
        assertThat(gqs.isEnchantment(gd, prayers)).isTrue();
        assertThat(gqs.getEffectivePower(gd, prayers)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, prayers)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, prayers)).contains(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, prayers, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Still gains life when already a creature")
    void stillGainsLifeWhenAlreadyCreature() {
        harness.setLife(player1, 20);
        Permanent prayers = harness.addToBattlefieldAndReturn(player1, new AnsweredPrayers());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gqs.isCreature(gd, prayers)).isTrue();
        assertThat(gqs.getEffectivePower(gd, prayers)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, prayers)).isEqualTo(3);
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent prayers = harness.addToBattlefieldAndReturn(player1, new AnsweredPrayers());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, prayers)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, prayers)).isFalse();
        assertThat(gqs.isEnchantment(gd, prayers)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, prayers)).doesNotContain(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, prayers, Keyword.FLYING)).isFalse();
    }
}
