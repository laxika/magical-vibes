package com.github.laxika.magicalvibes.cards.k;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KolaghanMonument.class})
class KolaghanMonumentTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Kolaghan Monument adds one black or red mana")
    void tappingAddsChosenMana() {
        Permanent monument = addReadyMonument();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(monument.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying four generic, black, and red mana animates Kolaghan Monument")
    void payingManaAnimatesMonument() {
        Permanent monument = addReadyMonument();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, monument)).isTrue();
        assertThat(gqs.isArtifact(monument)).isTrue();
        assertThat(gqs.getEffectivePower(gd, monument)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, monument)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, monument))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
        assertThat(monument.getTransientSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, monument, Keyword.FLYING)).isTrue();
        assertThat(monument.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Kolaghan Monument stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent monument = addReadyMonument();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

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

    @Test
    @DisplayName("A newly entered noncreature Monument can tap for red mana")
    void newlyEnteredMonumentAddsRedMana() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new KolaghanMonument());
        monument.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(monument.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Monument can animate without untapping")
    void tappedMonumentCanAnimate() {
        Permanent monument = addReadyMonument();
        monument.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gqs.isCreature(gd, monument)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, monument)).isTrue();
        assertThat(gqs.getEffectivePower(gd, monument)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, monument)).isEqualTo(4);
        assertThat(monument.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A newly entered Monument can animate but cannot then tap for mana")
    void newlyAnimatedMonumentHasSummoningSickness() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new KolaghanMonument());
        monument.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, monument)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(monument.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An animated Monument retains its mana ability")
    void animatedMonumentCanTapForMana() {
        Permanent monument = addReadyMonument();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gqs.isCreature(gd, monument)).isTrue();
        assertThat(monument.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyMonument() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new KolaghanMonument());
        monument.setSummoningSick(false);
        return monument;
    }
}
