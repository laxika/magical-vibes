package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Fleshgrafter;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UltronTheAnnihilator.class, Fleshgrafter.class, LeoninScimitar.class, LiquimetalCoating.class})
class UltronTheAnnihilatorTest extends BaseCardTest {

    @Test
    void entersAndAttacksToCreateRobotTokens() {
        addUltronReady();
        resolveAllTriggers();

        assertThat(robotTokens()).hasSize(1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(robotTokens()).hasSize(2);
        assertThat(robotTokens()).allSatisfy(token ->
                assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue());
    }

    @Test
    void artifactPermanentInOwnGraveyardMakesEachOpponentLoseLife() {
        harness.setLife(player2, 20);
        addUltronReady();
        resolveAllTriggers();
        Permanent robot = robotTokens().getFirst();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, robot));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void artifactCardDiscardedFromHandMakesEachOpponentLoseLife() {
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addUltronReady();
        resolveAllTriggers();
        Permanent fleshgrafter = harness.addToBattlefieldAndReturn(player1, new Fleshgrafter());
        harness.setHand(player1, List.of(new LeoninScimitar()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(fleshgrafter), null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void ownedArtifactDyingUnderOpponentControlStillDrains() {
        addUltronReady();
        resolveAllTriggers();
        LeoninScimitar card = new LeoninScimitar();
        card.setOwnerId(player1.getId());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, card);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    void opponentsArtifactDyingUnderOwnControlDoesNotDrain() {
        addUltronReady();
        resolveAllTriggers();
        LeoninScimitar card = new LeoninScimitar();
        card.setOwnerId(player2.getId());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, card);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertLife(player2, 20);
    }

    @Test
    void ownDeathDoesNotDrain() {
        Permanent ultron = addUltronReady();
        resolveAllTriggers();
        ultron.setMarkedDamage(4);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Ultron the Annihilator");
        harness.assertLife(player2, 20);
    }

    @Test
    void simultaneousDeathWithRobotDrainsOnlyForRobot() {
        Permanent ultron = addUltronReady();
        resolveAllTriggers();
        Permanent robot = robotTokens().getFirst();
        ultron.setMarkedDamage(4);
        robot.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Ultron the Annihilator");
        assertThat(robotTokens()).isEmpty();
        harness.assertLife(player2, 19);
    }

    @Test
    void nonartifactDeathDoesNotDrain() {
        addUltronReady();
        resolveAllTriggers();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Fleshgrafter());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }

    @Test
    void creatureMadeIntoArtifactDrainsWhenItDies() {
        addUltronReady();
        resolveAllTriggers();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Fleshgrafter());
        Permanent coating = harness.addToBattlefieldAndReturn(player1, new LiquimetalCoating());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(coating), null, creature.getId());
        resolveAllTriggers();
        assertThat(gqs.isArtifact(gd, creature)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Fleshgrafter");
        harness.assertLife(player2, 19);
    }

    @Test
    void discardingAnotherUltronCardDrains() {
        addUltronReady();
        resolveAllTriggers();
        Permanent fleshgrafter = harness.addToBattlefieldAndReturn(player1, new Fleshgrafter());
        harness.setHand(player1, List.of(new UltronTheAnnihilator()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(fleshgrafter), null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Ultron the Annihilator");
        harness.assertLife(player2, 19);
    }

    @Test
    void castingUltronCreatesOneUntappedNonattackingRobot() {
        harness.castFromHand(player1, new UltronTheAnnihilator(), "{3}{B}{B}");
        resolveAllTriggers();

        assertThat(robotTokens()).hasSize(1);
        Permanent robot = robotTokens().getFirst();
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(2);
        assertThat(robot.isTapped()).isFalse();
        assertThat(robot.isAttacking()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentsArtifactDeathDoesNotDrain() {
        addUltronReady();
        resolveAllTriggers();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertLife(player2, 20);
    }

    private Permanent addUltronReady() {
        Permanent ultron = harness.enterBattlefieldAndReturn(player1, new UltronTheAnnihilator());
        ultron.setSummoningSick(false);
        return ultron;
    }

    private List<Permanent> robotTokens() {
        return findPermanents(player1, "Robot").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
