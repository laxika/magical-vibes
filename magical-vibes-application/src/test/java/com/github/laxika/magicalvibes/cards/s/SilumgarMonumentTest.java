package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SilumgarMonument.class)
class SilumgarMonumentTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Silumgar Monument adds one blue or black mana")
    void tappingAddsChosenMana() {
        Permanent monument = addReadyMonument();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(monument.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying four generic, blue, and black mana animates Silumgar Monument")
    void payingManaAnimatesMonument() {
        Permanent monument = addReadyMonument();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, monument)).isTrue();
        assertThat(gqs.isArtifact(monument)).isTrue();
        assertThat(gqs.getEffectivePower(gd, monument)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, monument)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, monument))
                .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.BLACK);
        assertThat(monument.getTransientSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, monument, Keyword.FLYING)).isTrue();
        assertThat(monument.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Silumgar Monument stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent monument = addReadyMonument();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, monument)).isFalse();
        assertThat(gqs.isArtifact(monument)).isTrue();
        assertThat(monument.getTransientSubtypes()).doesNotContain(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, monument, Keyword.FLYING)).isFalse();
    }

    private Permanent addReadyMonument() {
        Permanent monument = new Permanent(new SilumgarMonument());
        monument.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(monument);
        return monument;
    }
}
