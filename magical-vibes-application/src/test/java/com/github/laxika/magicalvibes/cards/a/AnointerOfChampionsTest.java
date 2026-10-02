package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnointerOfChampions.class, TimberpackWolf.class})
class AnointerOfChampionsTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives an attacking creature +1/+1")
    void boostsAttackingCreature() {
        Permanent anointer = addCreatureReady(player1, new AnointerOfChampions());
        Permanent bears = addAttackingCreature(player1, new TimberpackWolf());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, bears.getId());
        assertThat(anointer.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new AnointerOfChampions());
        Permanent bears = addAttackingCreature(player1, new TimberpackWolf());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature that isn't attacking")
    void cannotTargetNonAttackingCreature() {
        addCreatureReady(player1, new AnointerOfChampions());
        Permanent bears = addCreatureReady(player2, new TimberpackWolf());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking");
    }

    @Test
    void canBoostOpponentsAttackingCreature() {
        addCreatureReady(player1, new AnointerOfChampions());
        Permanent wolf = addAttackingCreature(player2, new TimberpackWolf());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, wolf.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(3);
    }

    @Test
    void doesNotBoostTargetThatStopsAttackingBeforeResolution() {
        addCreatureReady(player1, new AnointerOfChampions());
        Permanent wolf = addAttackingCreature(player1, new TimberpackWolf());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, wolf.getId());
        wolf.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent anointer = addCreatureReady(player1, new AnointerOfChampions());
        anointer.setTapped(true);
        Permanent wolf = addAttackingCreature(player1, new TimberpackWolf());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, wolf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent anointer = addCreatureReady(player1, new AnointerOfChampions());
        anointer.setSummoningSick(true);
        Permanent wolf = addAttackingCreature(player1, new TimberpackWolf());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, wolf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(anointer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent anointer = addCreatureReady(player1, new AnointerOfChampions());
        Permanent wolf = addAttackingCreature(player1, new TimberpackWolf());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, wolf.getId());
        gd.playerBattlefields.get(player1.getId()).remove(anointer);
        gd.playerGraveyards.get(player1.getId()).add(anointer.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(3);
    }

    private Permanent addAttackingCreature(com.github.laxika.magicalvibes.model.Player player,
                                           com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.setAttacking(true);
        return perm;
    }
}
