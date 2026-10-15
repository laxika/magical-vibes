package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzoriusKeyrune.class})
class AzoriusKeyruneTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Azorius Keyrune adds one white or blue mana")
    void tappingAddsChosenMana() {
        Permanent keyrune = addReadyKeyrune(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(keyrune.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying white and blue mana animates Azorius Keyrune")
    void payingWhiteAndBlueAnimatesKeyrune() {
        Permanent keyrune = addReadyKeyrune(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, keyrune)).isTrue();
        assertThat(gqs.isArtifact(keyrune)).isTrue();
        assertThat(gqs.getEffectivePower(gd, keyrune)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, keyrune)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, keyrune))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(gqs.hasKeyword(gd, keyrune, Keyword.FLYING)).isTrue();
        assertThat(keyrune.getTransientSubtypes()).contains(CardSubtype.BIRD);
    }

    @Test
    @DisplayName("Azorius Keyrune stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent keyrune = addReadyKeyrune(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, keyrune)).isFalse();
        assertThat(gqs.isArtifact(keyrune)).isTrue();
        assertThat(gqs.hasKeyword(gd, keyrune, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A newly controlled noncreature Keyrune can tap for white mana")
    void newlyControlledKeyruneCanProduceWhiteMana() {
        Permanent keyrune = harness.addToBattlefieldAndReturn(player1, new AzoriusKeyrune());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(keyrune.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly controlled Keyrune can animate but cannot then tap for mana")
    void newlyControlledAnimatedKeyruneCannotTapForMana() {
        Permanent keyrune = harness.addToBattlefieldAndReturn(player1, new AzoriusKeyrune());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, keyrune)).isTrue();
        assertThat(keyrune.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(keyrune.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("An animated Keyrune retains its mana ability")
    void animatedKeyruneCanProduceMana() {
        Permanent keyrune = addReadyKeyrune(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.isCreature(gd, keyrune)).isTrue();
        assertThat(keyrune.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Keyrune can animate without untapping")
    void tappedKeyruneCanAnimate() {
        Permanent keyrune = addReadyKeyrune(player1);
        keyrune.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gqs.isCreature(gd, keyrune)).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, keyrune)).isTrue();
        assertThat(keyrune.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, keyrune)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, keyrune)).isEqualTo(2);
    }

    private Permanent addReadyKeyrune(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new AzoriusKeyrune());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
