package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChimericSphereTest extends BaseCardTest {

    @Test
    @DisplayName("First ability animates as 2/1 Construct with flying")
    void firstAbilityAnimatesAs2x1WithFlying() {
        Permanent sphere = addSphereReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sphere)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sphere)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sphere)).isEqualTo(1);
        assertThat(sphere.getTransientSubtypes()).containsExactly(CardSubtype.CONSTRUCT);
        assertThat(gqs.hasKeyword(gd, sphere, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Second ability animates as 3/2 Construct without flying")
    void secondAbilityAnimatesAs3x2WithoutFlying() {
        Permanent sphere = addSphereReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sphere)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sphere)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sphere)).isEqualTo(2);
        assertThat(sphere.getTransientSubtypes()).containsExactly(CardSubtype.CONSTRUCT);
        assertThat(gqs.hasKeyword(gd, sphere, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("First then second: 3/2 without flying")
    void firstThenSecondLosesFlying() {
        Permanent sphere = addSphereReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, sphere, Keyword.FLYING)).isTrue();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sphere)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sphere)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sphere, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Second then first: 2/1 with flying")
    void secondThenFirstGainsFlying() {
        Permanent sphere = addSphereReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, sphere, Keyword.FLYING)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sphere)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sphere)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, sphere, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent sphere = addSphereReady(player1);

        assertThat(gqs.isCreature(gd, sphere)).isFalse();
        assertThat(sphere.getCard().getType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Animation and flying reset at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent sphere = addSphereReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, sphere)).isTrue();
        assertThat(gqs.hasKeyword(gd, sphere, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sphere.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, sphere)).isFalse();
        assertThat(sphere.getTransientSubtypes()).isEmpty();
        assertThat(gqs.hasKeyword(gd, sphere, Keyword.FLYING)).isFalse();
    }

    private Permanent addSphereReady(Player player) {
        ChimericSphere card = new ChimericSphere();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
