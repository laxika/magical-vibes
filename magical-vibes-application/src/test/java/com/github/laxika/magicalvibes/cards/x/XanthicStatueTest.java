package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({XanthicStatue.class})
class XanthicStatueTest extends BaseCardTest {

    @Test
    @DisplayName("Xanthic Statue becomes an 8/8 Golem artifact creature with trample")
    void activatesAnimation() {
        Permanent statue = harness.addToBattlefieldAndReturn(player1, new XanthicStatue());
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
        Permanent statue = harness.addToBattlefieldAndReturn(player1, new XanthicStatue());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, statue)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.isCreature(gd, statue)).isFalse();
        assertThat(gqs.isArtifact(statue)).isTrue();
        assertThat(statue.getTransientSubtypes()).doesNotContain(CardSubtype.GOLEM);
        assertThat(statue.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Animation uses the stack and affects only its source")
    void animatesOnlyItsSourceOnResolution() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new XanthicStatue());
        Permanent statue = harness.addToBattlefieldAndReturn(player1, new XanthicStatue());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new XanthicStatue());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gqs.isCreature(gd, statue)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, statue)).isTrue();
        assertThat(gqs.getEffectivePower(gd, statue)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, statue)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, statue, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.isCreature(gd, other)).isFalse();
        assertThat(gqs.isCreature(gd, opposing)).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Statue can activate repeatedly without untapping")
    void tappedStatueCanActivateRepeatedly() {
        Permanent statue = harness.addToBattlefieldAndReturn(player1, new XanthicStatue());
        statue.setTapped(true);
        statue.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(statue.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, statue)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, statue)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, statue, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.isCreature(gd, statue)).isFalse();
        assertThat(gqs.hasKeyword(gd, statue, Keyword.TRAMPLE)).isFalse();
    }
}
