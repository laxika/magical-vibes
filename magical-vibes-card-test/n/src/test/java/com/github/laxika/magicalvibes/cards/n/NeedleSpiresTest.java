package com.github.laxika.magicalvibes.cards.n;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NeedleSpires.class})
class NeedleSpiresTest extends BaseCardTest {

    @Test
    @DisplayName("Needle Spires enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new NeedleSpires()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Needle Spires").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Needle Spires adds a chosen red or white mana")
    void tappingAddsChosenMana() {
        Permanent spires = addReadySpires(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(spires.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Needle Spires can produce white mana without using the stack")
    void tappingAddsWhiteManaImmediately() {
        Permanent spires = addReadySpires(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(spires.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Animation uses the stack and affects only the activated Needle Spires")
    void animationAffectsOnlyItsSourceOnResolution() {
        Permanent spires = addReadySpires(player1);
        Permanent other = addReadySpires(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, spires)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, spires)).isTrue();
        assertThat(spires.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, other)).isFalse();
    }

    @Test
    @DisplayName("A tapped Needle Spires can animate without untapping")
    void animatesWhileTapped() {
        Permanent spires = addReadySpires(player1);
        spires.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, spires)).isTrue();
        assertThat(spires.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Animated Needle Spires deals four damage when unblocked")
    void doubleStrikeDealsDamageInBothCombatDamageSteps() {
        addReadySpires(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Paying two generic, red, and white mana animates Needle Spires")
    void animatesIntoDoubleStrikeElemental() {
        Permanent spires = addReadySpires(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, spires)).isTrue();
        assertThat(gqs.isLand(gd, spires)).isTrue();
        assertThat(gqs.getEffectivePower(gd, spires)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spires)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, spires))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(spires.getTransientSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, spires, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Needle Spires stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent spires = addReadySpires(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, spires)).isFalse();
        assertThat(gqs.isLand(gd, spires)).isTrue();
        assertThat(gqs.hasKeyword(gd, spires, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent addReadySpires(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new NeedleSpires());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
