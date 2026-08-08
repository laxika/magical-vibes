package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XanthicStatueTest extends BaseCardTest {

    @Test
    @DisplayName("Xanthic Statue becomes an 8/8 Golem artifact creature with trample")
    void activatesAnimation() {
        Permanent statue = addStatueReady();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, statue)).isTrue();
        assertThat(gqs.isArtifact(statue)).isTrue();
        assertThat(gqs.getEffectivePower(gd, statue)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, statue)).isEqualTo(8);
        assertThat(statue.getTransientSubtypes()).contains(CardSubtype.GOLEM);
        assertThat(statue.getGrantedKeywords()).contains(Keyword.TRAMPLE);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Xanthic Statue's animation ends at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent statue = addStatueReady();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, statue)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, statue)).isFalse();
        assertThat(gqs.isArtifact(statue)).isTrue();
        assertThat(statue.getTransientSubtypes()).doesNotContain(CardSubtype.GOLEM);
        assertThat(statue.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    private Permanent addStatueReady() {
        Permanent statue = new Permanent(new XanthicStatue());
        statue.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(statue);
        return statue;
    }
}
