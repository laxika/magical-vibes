package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GarrukSavageHerald.class, GrizzlyBears.class, GarruksCompanion.class, Forest.class})
class GarrukSavageHeraldTest extends BaseCardTest {

    @Test
    void plusOnePutsARevealedCreatureIntoHand() {
        Permanent garruk = addReadyGarruk(3);
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void plusOnePutsANoncreatureOnTheBottom() {
        addReadyGarruk(3);
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, creature));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, land);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
    }

    @Test
    void minusTwoDealsSourcePowerToAnotherTargetCreature() {
        Permanent garruk = addReadyGarruk(3);
        Permanent source = addCreatureReady(player1, new GarruksCompanion());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(source.getId(), victim.getId()));
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(victim);
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .contains("Grizzly Bears");
    }

    @Test
    void minusTwoCannotTargetTheSameCreatureTwice() {
        addReadyGarruk(3);
        Permanent source = addCreatureReady(player1, new GarruksCompanion());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(source.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusSevenLetsBlockedCreaturesAssignDamageToThePlayer() {
        harness.setLife(player2, 20);
        addReadyGarruk(7);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        declareBlockedAttack(attacker, blocker);
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 2));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    private Permanent addReadyGarruk(int loyalty) {
        Permanent garruk = new Permanent(new GarrukSavageHerald());
        garruk.setCounterCount(CounterType.LOYALTY, loyalty);
        garruk.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(garruk);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return garruk;
    }

    private void declareBlockedAttack(Permanent attacker, Permanent blocker) {
        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
