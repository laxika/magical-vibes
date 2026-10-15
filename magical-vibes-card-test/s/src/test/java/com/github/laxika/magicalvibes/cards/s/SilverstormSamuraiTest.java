package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BileUrchin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilverstormSamurai.class, BileUrchin.class})
class SilverstormSamuraiTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows Silverstorm Samurai to enter during combat and block immediately")
    void flashedSamuraiCanBlockImmediately() {
        Permanent attacker = addCreatureReady(player1, new BileUrchin());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player2, new SilverstormSamurai(), "{4}{W}{W}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Silverstorm Samurai");
        Permanent samurai = findPermanent(player2, "Silverstorm Samurai");

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(samurai.getPowerModifier()).isEqualTo(1);
        assertThat(samurai.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flash allows Silverstorm Samurai to resolve before an opponent's pending spell")
    void canRespondToOpponentsSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BileUrchin(), "{B}");

        harness.castFromHand(player2, new SilverstormSamurai(), "{4}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Silverstorm Samurai");
        harness.assertNotOnBattlefield(player1, "Bile Urchin");

        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Bile Urchin");
    }

    @Test
    @DisplayName("When Silverstorm Samurai becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent samurai = addCreatureReady(player1, new SilverstormSamurai());
        samurai.setAttacking(true);
        addCreatureReady(player2, new BileUrchin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(samurai.getPowerModifier()).isEqualTo(1);
        assertThat(samurai.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Silverstorm Samurai blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addCreatureReady(player1, new BileUrchin());
        attacker.setAttacking(true);
        Permanent samurai = addCreatureReady(player2, new SilverstormSamurai());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(samurai.getPowerModifier()).isEqualTo(1);
        assertThat(samurai.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Silverstorm Samurai is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent samurai = addCreatureReady(player1, new SilverstormSamurai());
        samurai.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(samurai.getPowerModifier()).isZero();
        assertThat(samurai.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Bushido triggers only once when Silverstorm Samurai is blocked by multiple creatures")
    void multipleBlockersTriggerBushidoOnce() {
        Permanent samurai = addCreatureReady(player1, new SilverstormSamurai());
        samurai.setAttacking(true);
        addCreatureReady(player2, new BileUrchin());
        addCreatureReady(player2, new BileUrchin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(samurai.getPowerModifier()).isEqualTo(1);
        assertThat(samurai.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The Bushido bonus wears off at end of turn")
    void bushidoWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new BileUrchin());
        attacker.setAttacking(true);
        Permanent samurai = addCreatureReady(player2, new SilverstormSamurai());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(samurai.getPowerModifier()).isEqualTo(1);
        assertThat(samurai.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(samurai.getPowerModifier()).isZero();
        assertThat(samurai.getToughnessModifier()).isZero();
    }
}
