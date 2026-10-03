package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CyberneticaDatasmith.class, GrizzlyBears.class})
class CyberneticaDatasmithTest extends BaseCardTest {

    @Test
    void routesEffectsToTheirDifferentTargetPlayers() {
        addCreatureReady(player1, new CyberneticaDatasmith());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int player1HandSize = gd.playerHands.get(player1.getId()).size();
        int player2HandSize = gd.playerHands.get(player2.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbilityWithMultiTargets(
                player1,
                0,
                0,
                List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandSize + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandSize);

        List<Permanent> player1Robots = findPermanents(player1, "Robot");
        List<Permanent> player2Robots = findPermanents(player2, "Robot");
        assertThat(player1Robots).isEmpty();
        assertThat(player2Robots).hasSize(1);
        Permanent robot = player2Robots.getFirst();
        assertThat(robot.getCard().getSubtypes()).contains(CardSubtype.ROBOT);
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(4);
        assertThat(gqs.isArtifact(gd, robot)).isTrue();
    }

    @Test
    void requiresDifferentPlayersAsTargets() {
        addCreatureReady(player1, new CyberneticaDatasmith());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1,
                0,
                0,
                List.of(player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canGiveTheControllerTheRobotAndTheOpponentTheCard() {
        Permanent datasmith = addCreatureReady(player1, new CyberneticaDatasmith());
        harness.setLibrary(player2, List.of(new CyberneticaDatasmith()));
        int handSize = gd.playerHands.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player2.getId(), player1.getId()));
        assertThat(datasmith.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize + 1);
        assertThat(findPermanents(player1, "Robot")).hasSize(1);
        assertThat(findPermanents(player2, "Robot")).isEmpty();
    }

    @Test
    void createdRobotCannotBlock() {
        addCreatureReady(player1, new CyberneticaDatasmith());
        harness.setLibrary(player1, List.of(new CyberneticaDatasmith()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();
        Permanent attacker = addCreatureReady(player1, new CyberneticaDatasmith());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void protectionPreventsCombatDamageWhenBlockingCreatedRobot() {
        Permanent datasmith = addCreatureReady(player1, new CyberneticaDatasmith());
        harness.setLibrary(player1, List.of(new CyberneticaDatasmith()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();
        datasmith.untap();
        Permanent robot = findPermanent(player2, "Robot");
        robot.setSummoningSick(false);
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Cybernetica Datasmith");
        assertThat(datasmith.getMarkedDamage()).isZero();
    }

    @Test
    void summoningSickDatasmithCannotActivateTapAbility() {
        harness.addToBattlefield(player1, new CyberneticaDatasmith());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityCannotBeActivatedWithoutBlueMana() {
        addCreatureReady(player1, new CyberneticaDatasmith());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappedDatasmithCannotActivateAgain() {
        Permanent datasmith = addCreatureReady(player1, new CyberneticaDatasmith());
        datasmith.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
