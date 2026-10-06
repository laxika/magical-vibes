package com.github.laxika.magicalvibes.cards.o;

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

@CardUsed(OjutaiMonument.class)
class OjutaiMonumentTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Ojutai Monument adds one white or blue mana")
    void tappingAddsChosenMana() {
        Permanent monument = addReadyMonument();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(monument.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying four generic, white, and blue mana animates Ojutai Monument")
    void payingManaAnimatesMonument() {
        Permanent monument = addReadyMonument();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, monument)).isTrue();
        assertThat(gqs.isArtifact(monument)).isTrue();
        assertThat(gqs.getEffectivePower(gd, monument)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, monument)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, monument))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(monument.getTransientSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, monument, Keyword.FLYING)).isTrue();
        assertThat(monument.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ojutai Monument stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent monument = addReadyMonument();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

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
    @DisplayName("A newly entered noncreature Monument can tap for blue mana")
    void newlyEnteredMonumentAddsBlueMana() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new OjutaiMonument());
        monument.setSummoningSick(true);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(monument.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Monument can animate without untapping")
    void tappedMonumentCanAnimate() {
        Permanent monument = addReadyMonument();
        monument.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gqs.isCreature(gd, monument)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, monument)).isTrue();
        assertThat(monument.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("A newly entered Monument can animate but cannot then tap for mana")
    void animatedNewMonumentCannotTapForMana() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new OjutaiMonument());
        monument.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
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
    void animatedMonumentCanAddMana() {
        Permanent monument = addReadyMonument();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(monument.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, monument)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyMonument() {
        return addCreatureReady(player1, new OjutaiMonument());
    }
}
