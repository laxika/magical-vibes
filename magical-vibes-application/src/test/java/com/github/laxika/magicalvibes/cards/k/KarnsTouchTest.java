package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
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

class KarnsTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Animates a target noncreature artifact with P/T equal to its mana value")
    void animatesNoncreatureArtifact() {
        Permanent manipulator = addArtifact(player1);
        castKarnsTouch(manipulator);

        assertThat(manipulator.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, manipulator)).isTrue();
        assertThat(manipulator.getEffectivePower()).isEqualTo(4);
        assertThat(manipulator.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        Permanent myr = new Permanent(new IronMyr());
        gd.playerBattlefields.get(player2.getId()).add(myr);
        harness.setHand(player1, List.of(new KarnsTouch()));
        addBlueMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, myr.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent manipulator = addArtifact(player1);
        castKarnsTouch(manipulator);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(manipulator.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, manipulator)).isFalse();
    }

    private Permanent addArtifact(Player player) {
        Permanent permanent = new Permanent(new IcyManipulator());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void castKarnsTouch(Permanent target) {
        harness.setHand(player1, List.of(new KarnsTouch()));
        addBlueMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addBlueMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
    }
}
