package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({HalcyonGlaze.class, GrizzlyBears.class, Shock.class})
class HalcyonGlazeTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 4/4 Illusion with flying when you cast a creature spell")
    void animatesForCreatureSpell() {
        Permanent glaze = addGlaze();

        assertThat(gqs.isCreature(gd, glaze)).isFalse();
        assertThat(gqs.isEnchantment(gd, glaze)).isTrue();

        castCreatureSpell();

        assertThat(gqs.isCreature(gd, glaze)).isTrue();
        assertThat(gqs.isEnchantment(gd, glaze)).isTrue();
        assertThat(gqs.getEffectivePower(gd, glaze)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, glaze)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, glaze)).containsExactly(CardSubtype.ILLUSION);
        assertThat(gqs.hasKeyword(gd, glaze, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not animate when you cast a noncreature spell")
    void doesNotAnimateForNoncreatureSpell() {
        Permanent glaze = addGlaze();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, glaze)).isFalse();
        assertThat(gqs.isEnchantment(gd, glaze)).isTrue();
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent glaze = addGlaze();
        castCreatureSpell();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, glaze)).isFalse();
        assertThat(gqs.isEnchantment(gd, glaze)).isTrue();
        assertThat(gqs.hasKeyword(gd, glaze, Keyword.FLYING)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, glaze)).isEmpty();
    }

    private Permanent addGlaze() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return harness.addToBattlefieldAndReturn(player1, new HalcyonGlaze());
    }

    private void castCreatureSpell() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
