package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyshipStalker.class})
class SkyshipStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability gives Skyship Stalker +1/+0 until end of turn")
    void boostsSelf() {
        Permanent stalker = addStalkerReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(stalker.getPowerModifier()).isEqualTo(1);
        assertThat(stalker.getToughnessModifier()).isEqualTo(0);

        endTurn();

        assertThat(stalker.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The second ability grants first strike until end of turn")
    void grantsFirstStrike() {
        Permanent stalker = addStalkerReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.FIRST_STRIKE)).isTrue();

        endTurn();

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The third ability grants haste until end of turn")
    void grantsHaste() {
        Permanent stalker = addStalkerReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.HASTE)).isTrue();

        endTurn();

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Repeated boosts stack and affect only the activating Stalker")
    void repeatedBoostsAffectOnlySource() {
        Permanent stalker = addStalkerReady(player1);
        Permanent other = addStalkerReady(player1);
        Permanent opponent = addStalkerReady(player2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(stalker.getPowerModifier()).isZero();

        resolveAllTriggers();

        assertThat(stalker.getPowerModifier()).isEqualTo(2);
        assertThat(stalker.getToughnessModifier()).isZero();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();

        endTurn();

        assertThat(stalker.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("A newly entered Stalker can activate haste and attack that turn")
    void hasteAllowsAttackingWhileSummoningSick() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new SkyshipStalker());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SkyshipStalker());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThat(als.canAttack(gd, stalker, player1.getId())).isFalse();

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(als.canAttack(gd, stalker, player1.getId())).isFalse();

        harness.passBothPriorities();

        assertThat(als.canAttack(gd, stalker, player1.getId())).isTrue();
        assertThat(als.canAttack(gd, other, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("First strike can be activated while tapped and affects only its source")
    void firstStrikeDoesNotRequireTapping() {
        Permanent stalker = addStalkerReady(player1);
        Permanent other = addStalkerReady(player2);
        stalker.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(stalker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted first strike kills an equally sized blocker before it deals damage")
    void firstStrikeWinsCombatAgainstAnotherStalker() {
        Permanent attacker = addStalkerReady(player1);
        addStalkerReady(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof SkyshipStalker);
    }

    private Permanent addStalkerReady(Player player) {
        return addCreatureReady(player, new SkyshipStalker());
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
