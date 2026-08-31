package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeedingFrenzy.class, Forest.class, GrizzlyBears.class, HillGiant.class, WalkingCorpse.class})
class FeedingFrenzyTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets -X/-X for the number of Zombies on all battlefields")
    void shrinksForEachZombieOnBattlefields() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.addToBattlefield(player2, new WalkingCorpse());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new FeedingFrenzy()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isZero();
        assertThat(target.getEffectiveToughness()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("The -X/-X effect wears off at cleanup")
    void shrinkWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new FeedingFrenzy()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Feeding Frenzy cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FeedingFrenzy()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
