package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GoblinMaskmaker;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DueDiligence.class, GoblinMaskmaker.class})
class DueDiligenceTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the enchanted creature continuously and another creature until end of turn")
    void boostsEnchantedAndOtherCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GoblinMaskmaker());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GoblinMaskmaker());

        castDueDiligence(enchanted, other);

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target the enchanted creature for the enter-the-battlefield ability")
    void cannotTargetEnchantedCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GoblinMaskmaker());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GoblinMaskmaker());
        castAura(enchanted);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(other.getId()).doesNotContain(enchanted.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, enchanted.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The enter-the-battlefield target must be a creature you control")
    void cannotTargetOpponentCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GoblinMaskmaker());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GoblinMaskmaker());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GoblinMaskmaker());
        castAura(enchanted);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(other.getId()).doesNotContain(opponentCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can enchant your only creature without an eligible triggered-ability target")
    void canEnchantOnlyCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GoblinMaskmaker());
        castAura(enchanted);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Due Diligence");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Can enchant an opponent's creature and boost a creature controlled by the Aura's controller")
    void canEnchantOpponentCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new GoblinMaskmaker());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GoblinMaskmaker());

        castDueDiligence(enchanted, other);

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Chooses the triggered-ability target after the Aura resolves")
    void choosesTargetAfterAuraEnters() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GoblinMaskmaker());
        castAura(enchanted);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GoblinMaskmaker());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
        harness.handlePermanentChosen(player1, other.getId());
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isTrue();
    }

    private void castAura(Permanent enchanted) {
        harness.setHand(player1, List.of(new DueDiligence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, enchanted.getId());
    }

    private void castDueDiligence(Permanent enchanted, Permanent other) {
        castAura(enchanted);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();
    }
}
