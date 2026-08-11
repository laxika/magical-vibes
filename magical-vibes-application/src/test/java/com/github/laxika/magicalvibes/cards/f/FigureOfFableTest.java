package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FigureOfFableTest extends BaseCardTest {

    private Permanent addFigure() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return harness.addToBattlefieldAndReturn(player1, new FigureOfFable());
    }

    private void resetPriority() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void activate(Permanent figure, int abilityIndex, int mana) {
        harness.addMana(player1, ManaColor.GREEN, mana);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(figure);
        harness.activateAbility(player1, index, abilityIndex, null, null);
        harness.passBothPriorities();
    }

    private static Card createTargetedInstant() {
        Card card = new Card();
        card.setName("Opponent's Bolt");
        card.setType(CardType.INSTANT);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }

    @Test
    @DisplayName("First ability permanently makes Figure of Fable a 2/3")
    void firstAbilityChangesBaseStats() {
        Permanent figure = addFigure();

        activate(figure, 0, 1);

        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, figure)).isEqualTo(3);
    }

    @Test
    @DisplayName("Second ability requires Scout and then makes Figure of Fable a 4/5")
    void secondAbilityRequiresScout() {
        Permanent figure = addFigure();

        activate(figure, 1, 3);
        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, figure)).isEqualTo(1);

        resetPriority();
        activate(figure, 0, 1);
        resetPriority();
        activate(figure, 1, 3);

        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, figure)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, figure))
                .contains(CardSubtype.KITHKIN, CardSubtype.SOLDIER)
                .doesNotContain(CardSubtype.SCOUT);
    }

    @Test
    @DisplayName("Full chain reaches 7/8 and stops an opponent's targeted spell")
    void fullChainGrantsProtectionFromOpponents() {
        Permanent figure = addFigure();

        activate(figure, 0, 1);
        resetPriority();
        activate(figure, 1, 3);
        resetPriority();
        activate(figure, 2, 6);

        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, figure)).isEqualTo(8);
        assertThat(gqs.effectiveCreatureSubtypes(gd, figure))
                .contains(CardSubtype.KITHKIN, CardSubtype.AVATAR)
                .doesNotContain(CardSubtype.SOLDIER);

        harness.setHand(player2, List.of(createTargetedInstant()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, figure.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from the source's controller");
    }
}
