package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BorosKeyrune.class})
class BorosKeyruneTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Boros Keyrune adds one red or white mana")
    void tappingAddsChosenMana() {
        Permanent keyrune = addCreatureReady(player1, new BorosKeyrune());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(keyrune.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying red and white mana animates Boros Keyrune")
    void payingRedAndWhiteAnimatesKeyrune() {
        Permanent keyrune = addCreatureReady(player1, new BorosKeyrune());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, keyrune)).isTrue();
        assertThat(gqs.isArtifact(keyrune)).isTrue();
        assertThat(gqs.getEffectivePower(gd, keyrune)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, keyrune)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, keyrune))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(gqs.hasKeyword(gd, keyrune, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(keyrune.getTransientSubtypes()).contains(CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("Boros Keyrune stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent keyrune = addCreatureReady(player1, new BorosKeyrune());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, keyrune)).isFalse();
        assertThat(gqs.isArtifact(keyrune)).isTrue();
        assertThat(gqs.hasKeyword(gd, keyrune, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Boros Keyrune can produce red mana")
    void tappingAddsRedMana() {
        Permanent keyrune = harness.addToBattlefieldAndReturn(player1, new BorosKeyrune());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(keyrune.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Boros Keyrune can animate without untapping")
    void tappedKeyruneCanAnimate() {
        Permanent keyrune = harness.addToBattlefieldAndReturn(player1, new BorosKeyrune());
        keyrune.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gqs.isCreature(gd, keyrune)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, keyrune)).isTrue();
        assertThat(keyrune.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Animated Boros Keyrune retains its mana ability")
    void animatedKeyruneCanProduceMana() {
        Permanent keyrune = addCreatureReady(player1, new BorosKeyrune());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gqs.isCreature(gd, keyrune)).isTrue();
        assertThat(keyrune.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Animated Boros Keyrune deals both combat damage steps")
    void animatedKeyruneDealsDoubleStrikeDamage() {
        Permanent keyrune = addCreatureReady(player1, new BorosKeyrune());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        keyrune.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 18);
    }
}
