package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtherealGuidance.class, DevilthornFox.class})
class EtherealGuidanceTest extends BaseCardTest {

    @Test
    void resolvingBoostsOwnCreaturesButNotOpponents() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new EtherealGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(ownCreature.getPowerModifier()).isEqualTo(2);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(1);
        assertThat(opponentCreature.getPowerModifier()).isZero();
        assertThat(opponentCreature.getToughnessModifier()).isZero();
    }

    @Test
    void boostEndsAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        harness.setHand(player1, List.of(new EtherealGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    void boostsEveryCreaturePresentAtResolutionButNotLaterArrivals() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        harness.setHand(player1, List.of(new EtherealGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        Permanent second = harness.enterBattlefieldAndReturn(player1, new DevilthornFox());
        harness.passBothPriorities();
        Permanent later = harness.enterBattlefieldAndReturn(player1, new DevilthornFox());

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(second.getToughnessModifier()).isEqualTo(1);
        assertThat(later.getPowerModifier()).isZero();
        assertThat(later.getToughnessModifier()).isZero();
    }

    @Test
    void repeatedCastsAccumulateUntilCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        harness.setHand(player1, List.of(new EtherealGuidance(), new EtherealGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(creature.getPowerModifier()).isEqualTo(4);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    void resolvesWithNoCreaturesAndDoesNotBoostLaterArrivals() {
        harness.setHand(player1, List.of(new EtherealGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Ethereal Guidance");
        Permanent later = harness.enterBattlefieldAndReturn(player1, new DevilthornFox());
        assertThat(later.getPowerModifier()).isZero();
        assertThat(later.getToughnessModifier()).isZero();
    }
}
