package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoldDefenseTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, creatures you control get +1/+1")
    void withoutKickerBoostsOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BoldDefense()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(findCreature(player1).getEffectivePower()).isEqualTo(3);
        assertThat(findCreature(player1).getEffectiveToughness()).isEqualTo(3);
        assertThat(findCreature(player1).hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(findCreature(player2).getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("When kicked, creatures you control get +2/+2 and first strike")
    void kickedBoostsOwnCreaturesAndGrantsFirstStrike() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BoldDefense()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();

        Permanent ownCreature = findCreature(player1);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(4);
        assertThat(ownCreature.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(findCreature(player2).getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Kicked boost and first strike wear off at cleanup")
    void kickedEffectsWearOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BoldDefense()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent ownCreature = findCreature(player1);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(ownCreature.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent findCreature(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).getFirst();
    }
}
