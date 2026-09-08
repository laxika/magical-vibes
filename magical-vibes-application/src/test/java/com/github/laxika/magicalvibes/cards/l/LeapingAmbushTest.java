package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
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

@CardUsed({LeapingAmbush.class, GrizzlyBears.class, Pacifism.class})
class LeapingAmbushTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts, grants reach to, and untaps the target creature")
    void resolvesAllEffects() {
        Permanent target = addTappedCreature(player2);
        castLeapingAmbush(target);

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost and reach wear off at end of turn")
    void temporaryEffectsWearOff() {
        Permanent target = addTappedCreature(player2);
        castLeapingAmbush(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addTappedCreature(player1);
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        harness.setHand(player1, List.of(new LeapingAmbush()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castLeapingAmbush(Permanent target) {
        harness.setHand(player1, List.of(new LeapingAmbush()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addTappedCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        permanent.tap();
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
