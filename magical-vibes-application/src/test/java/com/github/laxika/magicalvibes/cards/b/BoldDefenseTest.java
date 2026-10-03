package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoldDefense.class, StoneworkPuma.class})
class BoldDefenseTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, creatures you control get +1/+1")
    void withoutKickerBoostsOwnCreatures() {
        harness.addToBattlefield(player1, new StoneworkPuma());
        harness.addToBattlefield(player2, new StoneworkPuma());
        harness.setHand(player1, List.of(new BoldDefense()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findCreature(player1).getEffectivePower()).isEqualTo(3);
        assertThat(findCreature(player1).getEffectiveToughness()).isEqualTo(3);
        assertThat(findCreature(player1).hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(findCreature(player2).getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("When kicked, creatures you control get +2/+2 and first strike")
    void kickedBoostsOwnCreaturesAndGrantsFirstStrike() {
        harness.addToBattlefield(player1, new StoneworkPuma());
        harness.addToBattlefield(player2, new StoneworkPuma());
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
        harness.addToBattlefield(player1, new StoneworkPuma());
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

    @Test
    @DisplayName("Unkicked boost wears off at cleanup")
    void unkickedBoostWearsOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());
        harness.setHand(player1, List.of(new BoldDefense()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Kicked spell affects creatures present at resolution, not later arrivals")
    void kickedSpellUsesCreaturesPresentAtResolution() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());
        harness.setHand(player1, List.of(new BoldDefense()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedInstant(player1, 0);
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new StoneworkPuma());
        harness.passBothPriorities();
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new StoneworkPuma());

        for (Permanent affected : List.of(original, beforeResolution)) {
            assertThat(affected.getEffectivePower()).isEqualTo(4);
            assertThat(affected.getEffectiveToughness()).isEqualTo(4);
            assertThat(affected.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        }
        assertThat(afterResolution.getEffectivePower()).isEqualTo(2);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(2);
        assertThat(afterResolution.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Bold Defense resolves with no creatures")
    void resolvesWithNoCreatures() {
        harness.setHand(player1, List.of(new BoldDefense()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Bold Defense");
        Permanent laterCreature = harness.enterBattlefieldAndReturn(player1, new StoneworkPuma());
        assertThat(laterCreature.getEffectivePower()).isEqualTo(2);
        assertThat(laterCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(laterCreature.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent findCreature(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).getFirst();
    }
}
