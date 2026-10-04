package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScorchingDragonfire;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeroicIntervention.class, GrizzlyBears.class, Forest.class, ScorchingDragonfire.class})
class HeroicInterventionTest extends BaseCardTest {

    @Test
    void grantsHexproofAndIndestructibleToAllOwnPermanents() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new HeroicIntervention()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        assertHasBothKeywords(ownCreature);
        assertHasBothKeywords(ownLand);
        assertThat(opponentCreature.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(opponentCreature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(opponentLand.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(opponentLand.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void grantsWearOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeroicIntervention()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);
        assertHasBothKeywords(ownCreature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(ownCreature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void onlyPermanentsPresentWhenSpellResolvesGainAbilities() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new HeroicIntervention()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertHasBothKeywords(original);
        assertHasBothKeywords(beforeResolution);
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void resolvesWithNoPermanentsWithoutProtectingLaterPermanents() {
        harness.setHand(player1, List.of(new HeroicIntervention()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);
        Permanent laterLand = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.assertInGraveyard(player1, "Heroic Intervention");
        assertThat(gqs.hasKeyword(gd, laterLand, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterLand, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void grantsRemainDuringEndStepBeforeCleanup() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new HeroicIntervention()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.forceStep(TurnStep.END_STEP);

        assertThat(gqs.hasKeyword(gd, ownLand, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownLand, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void hexproofInvalidatesOpponentsSpellAlreadyOnStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new ScorchingDragonfire()));
        harness.setHand(player1, List.of(new HeroicIntervention()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player2, 0, creature.getId());
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Scorching Dragonfire");
    }

    @Test
    void controllerCanTargetProtectedCreatureAndLethalDamageDoesNotDestroyIt() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeroicIntervention(), new ScorchingDragonfire()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Scorching Dragonfire");
    }

    private void assertHasBothKeywords(Permanent permanent) {
        assertThat(permanent.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(permanent.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
