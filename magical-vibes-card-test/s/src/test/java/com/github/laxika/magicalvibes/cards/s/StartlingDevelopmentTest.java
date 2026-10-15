package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StartlingDevelopment.class, GrizzlyBears.class, AlmightyBrushwagg.class})
class StartlingDevelopmentTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature becomes a blue 4/4 Serpent")
    void transformsTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasColor(gd, bears, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasColor(gd, bears, CardColor.GREEN)).isFalse();
        assertThat(bears.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.SERPENT);
    }

    @Test
    @DisplayName("The transformation wears off at end of turn")
    void transformationWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasColor(gd, bears, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasColor(gd, bears, CardColor.BLUE)).isFalse();
        assertThat(bears.getTransientCreatureTypeOverride()).isNull();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new StartlingDevelopment()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new StartlingDevelopment()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Startling Development");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new StartlingDevelopment()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Can transform an opponent's creature without removing trample")
    void transformsOpponentsCreatureAndRetainsAbilities() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());

        cast(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.SERPENT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.BRUSHWAGG)).isFalse();
    }

    @Test
    @DisplayName("Base stats do not overwrite an existing power and toughness bonus")
    void preservesExistingStatBonus() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        cast(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The transformed creature can still activate its original ability")
    void retainsActivatedAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        cast(target);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
    }

    @Test
    @DisplayName("Cycling discards immediately but draws only when the ability resolves")
    void cyclingPaysDiscardBeforeDrawing() {
        harness.setHand(player1, List.of(new StartlingDevelopment()));
        harness.setLibrary(player1, List.of(new StartlingDevelopment()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Startling Development");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Startling Development");
        assertThat(gd.stack).isEmpty();
    }
}
