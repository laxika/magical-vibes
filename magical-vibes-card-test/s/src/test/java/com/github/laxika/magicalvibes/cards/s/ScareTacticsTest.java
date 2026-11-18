package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScareTactics.class, RagingGoblin.class, Spellbook.class})
class ScareTacticsTest extends BaseCardTest {

    @Test
    void boostsCreaturesYouControl() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new ScareTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(1);
    }

    @Test
    void doesNotBoostOpponentsCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new ScareTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
    }

    @Test
    void boostExpiresAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new ScareTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void doesNotBoostNoncreaturesYouControl() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new ScareTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, noncreature)).isZero();
    }

    @Test
    void doesNotBoostCreaturesEnteringAfterResolution() {
        Permanent creatureBeforeResolution = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new ScareTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        Permanent creatureAfterResolution = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());

        assertThat(gqs.getEffectivePower(gd, creatureBeforeResolution)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creatureAfterResolution)).isEqualTo(1);
    }

    @Test
    void boostsCreaturesEnteringBeforeResolution() {
        harness.setHand(player1, List.of(new ScareTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void resolvesWithoutAnyCreatures() {
        harness.setHand(player1, List.of(new ScareTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Scare Tactics");
        assertThat(gd.stack).isEmpty();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
    }

    @Test
    void multipleCastsGiveCumulativeBoostsUntilCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new ScareTactics(), new ScareTactics()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }
}
