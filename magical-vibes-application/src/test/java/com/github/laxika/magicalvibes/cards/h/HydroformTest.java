package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);
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

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addLand(Player player) {
        Permanent permanent = new Permanent(new Forest());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
