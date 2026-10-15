package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CoralCommando;
import com.github.laxika.magicalvibes.cards.g.GrotesqueDemise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlagueWight.class, AirElemental.class, CoralCommando.class, GrotesqueDemise.class})
class PlagueWightTest extends BaseCardTest {

    @Test
    @DisplayName("When Plague Wight becomes blocked, each blocker gets -1/-1 until end of turn")
    void becomesBlockedShrinksEachBlocker() {
        Permanent wight = addCreatureReady(player1, new PlagueWight());
        wight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AirElemental());

        declareBlockers(wight, List.of(blocker));
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(-1);
        assertThat(blocker.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Multiple blockers each get -1/-1")
    void multipleBlockersEachShrink() {
        Permanent wight = addCreatureReady(player1, new PlagueWight());
        wight.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new AirElemental());
        Permanent secondBlocker = addCreatureReady(player2, new AirElemental());

        declareBlockers(wight, List.of(firstBlocker, secondBlocker));
        resolveAllTriggers();

        assertThat(firstBlocker.getPowerModifier()).isEqualTo(-1);
        assertThat(firstBlocker.getToughnessModifier()).isEqualTo(-1);
        assertThat(secondBlocker.getPowerModifier()).isEqualTo(-1);
        assertThat(secondBlocker.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("The -1/-1 effect wears off at end of turn")
    void shrinkWearsOffAtEndOfTurn() {
        Permanent wight = addCreatureReady(player1, new PlagueWight());
        wight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AirElemental());

        declareBlockers(wight, List.of(blocker));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Multiple blockers cause only one becomes-blocked trigger")
    void multipleBlockersCauseOneTrigger() {
        Permanent wight = addCreatureReady(player1, new PlagueWight());
        wight.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new CoralCommando());
        Permanent secondBlocker = addCreatureReady(player2, new CoralCommando());

        declareBlockers(wight, List.of(firstBlocker, secondBlocker));

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("One resolution shrinks all blockers, but not other creatures or Plague Wight")
    void oneResolutionShrinksAllBlockers() {
        Permanent wight = addCreatureReady(player1, new PlagueWight());
        wight.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new CoralCommando());
        Permanent secondBlocker = addCreatureReady(player2, new CoralCommando());
        Permanent bystander = addCreatureReady(player2, new CoralCommando());

        declareBlockers(wight, List.of(firstBlocker, secondBlocker));
        harness.passBothPriorities();

        assertThat(firstBlocker.getPowerModifier()).isEqualTo(-1);
        assertThat(firstBlocker.getToughnessModifier()).isEqualTo(-1);
        assertThat(secondBlocker.getPowerModifier()).isEqualTo(-1);
        assertThat(secondBlocker.getToughnessModifier()).isEqualTo(-1);
        assertThat(bystander.getPowerModifier()).isZero();
        assertThat(bystander.getToughnessModifier()).isZero();
        assertThat(wight.getPowerModifier()).isZero();
        assertThat(wight.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A one-toughness blocker dies before damage and Plague Wight stays blocked")
    void deadBlockerDoesNotLetWightDamageDefender() {
        Permanent wight = addCreatureReady(player1, new PlagueWight());
        wight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new PlagueWight());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareBlockers(wight, List.of(blocker));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wight);
    }

    @Test
    @DisplayName("Blockers still shrink if Plague Wight leaves before its trigger resolves")
    void triggerResolvesAfterWightIsExiled() {
        Permanent wight = addCreatureReady(player1, new PlagueWight());
        wight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CoralCommando());
        harness.setHand(player1, List.of(new GrotesqueDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareBlockers(wight, List.of(blocker));
        harness.castAndResolveInstant(player1, 0, wight.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wight);
        resolveAllTriggers();

        assertThat(blocker.getPowerModifier()).isEqualTo(-1);
        assertThat(blocker.getToughnessModifier()).isEqualTo(-1);
    }

    private void declareBlockers(Permanent wight, List<Permanent> blockers) {
        prepareDeclareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wight);
        gs.declareBlockers(gd, player2, blockers.stream()
                .map(blocker -> new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker), attackerIndex))
                .toList());
    }
}
