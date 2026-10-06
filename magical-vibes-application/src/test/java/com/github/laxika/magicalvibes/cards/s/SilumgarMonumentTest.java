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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilumgarMonument.class})
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

    @Test
    @DisplayName("Silumgar Monument can add black mana")
    void tappingAddsBlackMana() {
        Permanent monument = addReadyMonument();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(monument.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Silumgar Monument can animate without untapping")
    void tappedMonumentCanAnimate() {
        Permanent monument = addReadyMonument();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, monument)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, monument)).isTrue();
        assertThat(gqs.getEffectivePower(gd, monument)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, monument, Keyword.FLYING)).isTrue();
        assertThat(monument.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An animated Silumgar Monument retains its mana ability")
    void animatedMonumentCanProduceMana() {
        Permanent monument = addReadyMonument();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, monument)).isTrue();
        assertThat(monument.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("A newly controlled noncreature Monument can produce mana")
    void newNoncreatureMonumentCanProduceMana() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new SilumgarMonument());
        monument.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(monument.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A newly controlled Monument can animate but cannot then tap for mana")
    void newlyAnimatedMonumentIsSummoningSick() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new SilumgarMonument());
        monument.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, monument)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(monument.isTapped()).isFalse();
    }
    private Permanent addReadyMonument() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new SilumgarMonument());
        monument.setSummoningSick(false);
        return monument;
    }
}
