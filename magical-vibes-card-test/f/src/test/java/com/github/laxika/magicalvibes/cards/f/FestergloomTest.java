package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Festergloom.class, BlackCat.class, FugitiveWizard.class, RuneclawBear.class, Ornithopter.class})
class FestergloomTest extends BaseCardTest {

    @Test
    @DisplayName("Gives -1/-1 to nonblack creatures controlled by both players")
    void debuffsNonblackCreatures() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
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
    @DisplayName("Nonblack creatures reduced to 0 toughness die")
    void killsSmallNonblackCreatures() {
        harness.addToBattlefield(player2, new FugitiveWizard());

        castFestergloom();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        castFestergloom();
        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Colorless artifact creatures are nonblack and receive the penalty")
    void affectsColorlessCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castFestergloom();

        assertThat(creature.getEffectivePower()).isEqualTo(-1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the penalty")
    void doesNotAffectLaterCreatures() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castFestergloom();

        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent later = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(later.getId()).isNotEqualTo(original.getId());
        assertThat(later.getEffectivePower()).isEqualTo(2);
        assertThat(later.getEffectiveToughness()).isEqualTo(2);
        assertThat(original.getEffectivePower()).isEqualTo(1);
        assertThat(original.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Penalties from two casts accumulate and kill a nonblack 2/2")
    void repeatedCastsAccumulate() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player2, new BlackCat());

        castFestergloom();
        castFestergloom();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertOnBattlefield(player2, "Black Cat");
        harness.assertNotInGraveyard(player2, "Black Cat");
    }

    @Test
    @DisplayName("Resolves without targets on an empty battlefield")
    void resolvesOnEmptyBattlefield() {
        castFestergloom();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Festergloom");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private void castFestergloom() {
        harness.setHand(player1, List.of(new Festergloom()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
