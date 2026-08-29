package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed(ThunderTotem.class)
class ThunderTotemTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Thunder Totem adds white mana")
    void tappingAddsWhiteMana() {
        Permanent totem = addReadyTotem();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(totem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Thunder Totem becomes a white 2/2 Spirit artifact creature with flying and first strike")
    void animatesIntoWhiteSpirit() {
        Permanent totem = addReadyTotem();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, totem)).isTrue();
        assertThat(gqs.isArtifact(totem)).isTrue();
        assertThat(gqs.getEffectivePower(gd, totem)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, totem)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, totem)).containsExactly(CardColor.WHITE);
        assertThat(totem.getTransientSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, totem, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, totem, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Thunder Totem's animation ends at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent totem = addReadyTotem();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, totem)).isFalse();
        assertThat(totem.getTransientSubtypes()).doesNotContain(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, totem, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, totem, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addReadyTotem() {
        Permanent totem = new Permanent(new ThunderTotem());
        totem.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(totem);
        return totem;
    }
}
