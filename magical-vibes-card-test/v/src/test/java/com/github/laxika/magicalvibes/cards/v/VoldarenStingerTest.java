package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(VoldarenStinger.class)
class VoldarenStingerTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike only while attacking")
    void firstStrikeWhileAttacking() {
        Permanent stinger = addStinger();

        assertThat(gqs.hasKeyword(gd, stinger, Keyword.FIRST_STRIKE)).isFalse();

        stinger.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, stinger, Keyword.FIRST_STRIKE)).isTrue();

        stinger.setAttacking(false);
        assertThat(gqs.hasKeyword(gd, stinger, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Activation gives +2/+0 until end of turn")
    void activationBoostsSelf() {
        Permanent stinger = addStinger();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, battlefieldIndex(stinger), null, null);
        harness.passBothPriorities();

        assertThat(stinger.getPowerModifier()).isEqualTo(2);
        assertThat(stinger.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activation boost wears off at end of turn")
    void activationBoostWearsOffAtEndOfTurn() {
        Permanent stinger = addStinger();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, battlefieldIndex(stinger), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(stinger.getPowerModifier()).isEqualTo(0);
        assertThat(stinger.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("An attacking Stinger kills a blocking Stinger before it can deal damage")
    void attackingStingerWinsCombatAgainstBlockingStinger() {
        Permanent attacker = addStinger();
        Permanent blocker = addCreatureReady(player2, new VoldarenStinger());

        declareAttackersAndPrepareBlockers(List.of(battlefieldIndex(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, battlefieldIndex(attacker))));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Repeated activations accumulate and boost only their source")
    void repeatedActivationsBoostOnlyTheirSource() {
        Permanent stinger = addStinger();
        Permanent other = addStinger();
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, battlefieldIndex(stinger), null, null);
        harness.activateAbility(player1, battlefieldIndex(stinger), null, null);
        assertThat(stinger.getPowerModifier()).isZero();
        resolveAllTriggers();

        assertThat(stinger.getPowerModifier()).isEqualTo(4);
        assertThat(stinger.getToughnessModifier()).isZero();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Stinger can activate using generic mana and one red mana")
    void activationNeedsNeitherTappingNorHaste() {
        Permanent stinger = addStinger();
        stinger.setSummoningSick(true);
        stinger.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, battlefieldIndex(stinger), null, null);
        harness.passBothPriorities();

        assertThat(stinger.getPowerModifier()).isEqualTo(2);
        assertThat(stinger.getToughnessModifier()).isZero();
        assertThat(stinger.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activation cannot be paid with only colorless mana")
    void activationRequiresRedMana() {
        Permanent stinger = addStinger();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(stinger), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(stinger.getPowerModifier()).isZero();
    }

    private Permanent addStinger() {
        return addCreatureReady(player1, new VoldarenStinger());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
