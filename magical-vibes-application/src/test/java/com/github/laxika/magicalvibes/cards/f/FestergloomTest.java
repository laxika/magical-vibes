package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FestergloomTest extends BaseCardTest {

    @Test
    @DisplayName("Gives -1/-1 to nonblack creatures controlled by both players")
    void debuffsNonblackCreatures() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent black = harness.addToBattlefieldAndReturn(player2, new BlackCat());

        castFestergloom();

        assertThat(own.getEffectivePower()).isEqualTo(1);
        assertThat(own.getEffectiveToughness()).isEqualTo(1);
        assertThat(opponent.getEffectivePower()).isEqualTo(1);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(1);
        assertThat(black.getEffectivePower()).isEqualTo(1);
        assertThat(black.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Destroys nonblack creatures reduced to 0 toughness")
    void killsSmallNonblackCreatures() {
        harness.addToBattlefield(player2, new FugitiveWizard());

        castFestergloom();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castFestergloom();
        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    private void castFestergloom() {
        harness.setHand(player1, List.of(new Festergloom()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
