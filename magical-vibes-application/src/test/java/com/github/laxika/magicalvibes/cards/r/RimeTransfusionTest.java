package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RimeTransfusionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+1")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1);
        attachAura(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Snow mana lets the enchanted creature be blocked only by snow creatures")
    void snowManaRestrictsBlockersToSnowCreatures() {
        Permanent attacker = addCreatureReady(player1);
        attachAura(attacker);
        activateEvasion(attacker);
        attacker.setAttacking(true);

        Permanent nonsnowBlocker = addCreatureReady(player2);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(nonsnowBlocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)
        )))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("snow creatures");
    }

    @Test
    @DisplayName("A snow creature can block after the evasion ability resolves")
    void snowCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1);
        attachAura(attacker);
        activateEvasion(attacker);
        attacker.setAttacking(true);

        Permanent snowBlocker = addSnowCreature(player2);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(snowBlocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)
        )));

        assertThat(snowBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The evasion restriction wears off at end of turn")
    void evasionRestrictionWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1);
        attachAura(attacker);
        activateEvasion(attacker);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent blocker = addCreatureReady(player2);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)
        )));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Regular mana cannot pay the snow activation")
    void regularManaCannotPaySnowActivation() {
        Permanent creature = addCreatureReady(player1);
        attachAura(creature);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent addSnowCreature(Player player) {
        Permanent creature = addCreatureReady(player);
        TestCards.mutableCard(creature).setSupertypes(EnumSet.of(CardSupertype.SNOW));
        return creature;
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = new Permanent(new RimeTransfusion());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    private void activateEvasion(Permanent creature) {
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(creature),
                0,
                null,
                null);
        harness.passBothPriorities();
    }
}
