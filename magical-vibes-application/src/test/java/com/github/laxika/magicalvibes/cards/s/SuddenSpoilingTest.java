package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuddenSpoiling.class, SerraAngel.class, GrizzlyBears.class, FountainOfYouth.class})
class SuddenSpoilingTest extends BaseCardTest {

    @Test
    @DisplayName("Makes all creatures controlled by the target player 0/2 without abilities")
    void spoilsTargetPlayersCreatures() {
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent targetNoncreature = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castSuddenSpoiling(player2.getId());

        assertThat(targetCreature.getEffectivePower()).isZero();
        assertThat(targetCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, targetCreature, Keyword.FLYING)).isFalse();
        assertThat(targetNoncreature.getEffectiveToughness()).isZero();
        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSuddenSpoiling(player2.getId());
        assertThat(creature.getEffectivePower()).isZero();
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-player object")
    void cannotTargetNonPlayer() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SuddenSpoiling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID fountainId = fountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell can only target players");
    }

    private void castSuddenSpoiling(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new SuddenSpoiling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }
}
