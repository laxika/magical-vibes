package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.ScabClanCharger;
import com.github.laxika.magicalvibes.cards.s.StompingGround;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hydroform.class, StompingGround.class, ScabClanCharger.class})
class HydroformTest extends BaseCardTest {

    @Test
    @DisplayName("Animates target land into a 3/3 Elemental with flying that is still a land")
    void animatesTargetLand() {
        Permanent land = addLand(player1);
        castHydroform(land);

        assertThat(land.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(land.getTransientSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, land, Keyword.FLYING)).isTrue();
        assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("Can animate a land an opponent controls")
    void animatesOpponentLand() {
        Permanent land = addLand(player2);
        castHydroform(land);

        assertThat(gqs.isCreature(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOff() {
        Permanent land = addLand(player1);
        castHydroform(land);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(land.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(land.getTransientSubtypes()).doesNotContain(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonLand() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new ScabClanCharger());
        harness.setHand(player1, List.of(new Hydroform()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castHydroform(Permanent target) {
        harness.setHand(player1, List.of(new Hydroform()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Preserves the land's basic land types while granting Elemental")
    void preservesLandTypes() {
        Permanent land = addLand(player1);
        castHydroform(land);

        assertThat(gqs.effectiveLandTypes(gd, land))
                .contains(CardSubtype.MOUNTAIN, CardSubtype.FOREST);
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
    }

    @Test
    @DisplayName("Counters apply on top of the animated base power and toughness")
    void countersModifyAnimatedStats() {
        Permanent land = addLand(player1);
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        castHydroform(land);

        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(5);
    }

    @Test
    @DisplayName("Flying expires along with the animation")
    void flyingExpires() {
        Permanent land = addLand(player1);
        castHydroform(land);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, land, Keyword.FLYING)).isFalse();
        assertThat(gqs.effectiveLandTypes(gd, land))
                .contains(CardSubtype.MOUNTAIN, CardSubtype.FOREST);
    }

    @Test
    @DisplayName("Does not animate a new object when the targeted land leaves and returns")
    void targetLeavesBeforeResolution() {
        Permanent land = addLand(player1);
        harness.setHand(player1, List.of(new Hydroform()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, land.getId());

        gd.playerBattlefields.get(player1.getId()).remove(land);
        Permanent returnedLand = harness.addToBattlefieldAndReturn(player1, land.getCard());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, returnedLand)).isFalse();
        assertThat(gqs.hasKeyword(gd, returnedLand, Keyword.FLYING)).isFalse();
        harness.assertInGraveyard(player1, "Hydroform");
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new StompingGround());
    }
}
