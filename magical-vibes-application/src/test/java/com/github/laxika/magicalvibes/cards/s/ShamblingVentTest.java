package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ShamblingVent.class)
class ShamblingVentTest extends BaseCardTest {

    @Test
    @DisplayName("Shambling Vent enters tapped and adds white or black mana")
    void entersTappedAndAddsChosenMana() {
        harness.setHand(player1, List.of(new ShamblingVent()));
        harness.playLand(player1, 0);

        Permanent vent = findPermanent(player1, "Shambling Vent");
        assertThat(vent.isTapped()).isTrue();

        vent.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Shambling Vent enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new ShamblingVent()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Shambling Vent").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Shambling Vent taps for white or black mana")
    void tapsForWhiteOrBlackMana() {
        addVentReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);

        Permanent vent = gd.playerBattlefields.get(player1.getId()).getFirst();
        vent.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Shambling Vent becomes a 2/3 white and black Elemental with lifelink")
    void becomesAnimatedWithLifelink() {
        Permanent vent = addVentReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, vent)).isTrue();
        assertThat(gqs.isCreature(gd, vent)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vent)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, vent))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, vent)).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(vent.getTransientSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, vent, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Shambling Vent stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent vent = addVentReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vent)).isFalse();
        assertThat(vent.getTransientSubtypes()).doesNotContain(CardSubtype.ELEMENTAL);
        assertThat(gqs.isLand(gd, vent)).isTrue();
        assertThat(gqs.hasKeyword(gd, vent, Keyword.LIFELINK)).isFalse();
    }

    private Permanent addVentReady(Player player) {
        Permanent vent = new Permanent(new ShamblingVent());
        vent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(vent);
        return vent;
    }
}
