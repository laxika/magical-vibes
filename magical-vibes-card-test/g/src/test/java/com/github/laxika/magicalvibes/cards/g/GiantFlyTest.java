package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.n.NantukoHusk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantFly.class, AuraOfSilence.class, AngelsFeather.class, NantukoHusk.class})
class GiantFlyTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when you sacrifice another permanent")
    void getsPowerForSacrificedPermanent() {
        Permanent giantFly = addCreatureReady(player1, new GiantFly());
        harness.addToBattlefield(player1, new AuraOfSilence());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());

        harness.sacrificePermanent(player1, 1, artifact.getId());
        resolveAllTriggers();

        assertThat(harness.getGameQueryService().getEffectivePower(gd, giantFly)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, giantFly)).isEqualTo(2);
    }

    @Test
    @DisplayName("The sacrifice bonus expires at end of turn")
    void bonusExpiresAtEndOfTurn() {
        Permanent giantFly = addCreatureReady(player1, new GiantFly());
        harness.addToBattlefield(player1, new AuraOfSilence());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());

        harness.sacrificePermanent(player1, 1, artifact.getId());
        resolveAllTriggers();

        assertThat(harness.getGameQueryService().getEffectivePower(gd, giantFly)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(harness.getGameQueryService().getEffectivePower(gd, giantFly)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's sacrifice does not boost Giant Fly")
    void opponentSacrificeDoesNotBoost() {
        Permanent giantFly = addCreatureReady(player1, new GiantFly());
        harness.addToBattlefield(player2, new AuraOfSilence());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());

        harness.sacrificePermanent(player2, 0, artifact.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, giantFly)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, giantFly)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each sacrifice gives a separate cumulative bonus")
    void repeatedSacrificesAccumulate() {
        Permanent giantFly = addCreatureReady(player1, new GiantFly());
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addToBattlefield(player1, new AuraOfSilence());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());

        harness.sacrificePermanent(player1, 1, firstArtifact.getId());
        resolveAllTriggers();
        harness.sacrificePermanent(player1, 1, secondArtifact.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, giantFly)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giantFly)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing another Giant Fly triggers only the surviving copy")
    void sacrificingAnotherCopyDoesNotTriggerItself() {
        addCreatureReady(player1, new NantukoHusk());
        Permanent survivor = addCreatureReady(player1, new GiantFly());
        Permanent sacrificed = addCreatureReady(player1, new GiantFly());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());

        assertThat(gd.stack.stream()
                .filter(entry -> survivor.getId().equals(entry.getSourcePermanentId()))).hasSize(1);
        assertThat(gd.stack.stream()
                .filter(entry -> entry.getCard() instanceof GiantFly)).hasSize(1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificed);
    }
}
