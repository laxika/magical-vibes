package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MorphlingTest extends BaseCardTest {

    @Test
    @DisplayName("Untap ability untaps Morphling, including when it is tapped")
    void untapAbilityUntapsMorphling() {
        Permanent morphling = addMorphlingReady(player1);
        morphling.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(morphling.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flying and shroud abilities grant their keywords until end of turn")
    void keywordAbilitiesGrantKeywordsUntilEndOfTurn() {
        Permanent morphling = addMorphlingReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, morphling, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, morphling, Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, morphling, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, morphling, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Power and toughness abilities apply their respective temporary modifiers")
    void powerAndToughnessAbilitiesModifyMorphling() {
        Permanent morphling = addMorphlingReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, morphling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, morphling)).isEqualTo(2);

        harness.activateAbility(player1, 0, 4, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, morphling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, morphling)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power and toughness modifiers wear off at end of turn")
    void powerAndToughnessModifiersWearOff() {
        Permanent morphling = addMorphlingReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, morphling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, morphling)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, morphling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, morphling)).isEqualTo(3);
    }

    private Permanent addMorphlingReady(Player player) {
        Permanent morphling = new Permanent(new Morphling());
        morphling.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(morphling);
        return morphling;
    }
}
