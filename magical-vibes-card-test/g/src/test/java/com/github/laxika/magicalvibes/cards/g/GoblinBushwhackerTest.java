package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@DisplayName("Goblin Bushwhacker")
@CardUsed({GoblinBushwhacker.class, GrizzlyBears.class, Unsummon.class})
class GoblinBushwhackerTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, it has no ETB effect")
    void withoutKickerHasNoEtbEffect() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoblinBushwhacker()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When kicked, it boosts and gives haste to creatures you control")
    void kickedBoostsOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GoblinBushwhacker()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bushwhacker = findPermanent(player1, "Goblin Bushwhacker");
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bushwhacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bushwhacker, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The kicked boost and haste expire at end of turn")
    void kickedEffectExpiresAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GoblinBushwhacker()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The trigger affects creatures present at resolution, but not later arrivals")
    void creaturesAreDeterminedAtResolution() {
        harness.setHand(player1, List.of(new GoblinBushwhacker()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.HASTE)).isFalse();

        harness.passBothPriorities();

        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The kicked trigger resolves after Bushwhacker leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoblinBushwhacker()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        Permanent bushwhacker = findPermanent(player1, "Goblin Bushwhacker");
        harness.castAndResolveInstant(player2, 0, bushwhacker.getId());
        harness.assertNotOnBattlefield(player1, "Goblin Bushwhacker");
        harness.assertInHand(player1, "Goblin Bushwhacker");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast does not trigger the kicked ability")
    void enteringWithoutCastingDoesNotTrigger() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent bushwhacker = harness.enterBattlefieldAndReturn(player1, new GoblinBushwhacker());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bushwhacker)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bushwhacker, Keyword.HASTE)).isFalse();
    }
}
