package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HedronFieldsOfAgadeem.class, GrizzlyBears.class})
class HedronFieldsOfAgadeemTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new HedronFieldsOfAgadeem(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void creaturesWithPowerSevenOrGreaterCannotAttack() {
        Permanent attacker = addCreatureReady(player1, creature("Seven Power", 7, 7));

        assertThatThrownBy(() -> declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(attacker.isAttacking()).isFalse();
    }

    @Test
    void creaturesWithPowerSevenOrGreaterCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, creature("Seven Power", 7, 7));

        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void chaosCreatesSevenSevenEldraziWithAnnihilatorOne() {
        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Eldrazi").getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(7);

        gd.planechase.faceUp.clear();
        token.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(token)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void creatureWithEffectivePowerSixCanAttack() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setPowerModifier(4);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    void creatureWithEffectivePowerSixCanBlock() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setPowerModifier(4);
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void increasedPowerIsUsedForAttackRestriction() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setPowerModifier(5);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(attacker.isAttacking()).isFalse();
    }

    @Test
    void increasedPowerIsUsedForBlockRestriction() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setPowerModifier(5);
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void restrictionAlsoPreventsOpponentsCreaturesWithPowerAboveSevenFromAttacking() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setPowerModifier(6);
        gd.planechase.controllerId = player2.getId();

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(attacker.isAttacking()).isFalse();
    }

    @Test
    void chaosCreatesTokenForCurrentPlanarController() {
        harness.forceActivePlayer(player2);
        gd.planechase.controllerId = player2.getId();

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Eldrazi")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void chaosTokenCannotAttackWhilePlaneRemainsFaceUp() {
        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();
        Permanent token = findPermanent(player1, "Eldrazi");
        token.setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(token.isAttacking()).isFalse();
    }

    @Test
    void reducingTokenPowerAllowsAttackAndDefenderChoosesExactlyOnePermanentToSacrifice() {
        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();
        Permanent token = findPermanent(player1, "Eldrazi");
        token.setSummoningSick(false);
        token.setPowerModifier(-1);
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
    }

    private static Card creature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{" + power + "}");
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
