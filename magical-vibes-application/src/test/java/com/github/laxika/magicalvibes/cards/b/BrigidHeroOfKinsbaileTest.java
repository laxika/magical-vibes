package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.c.CennsHeir;
import com.github.laxika.magicalvibes.cards.c.CloudgoatRanger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrigidHeroOfKinsbaile.class, AvianChangeling.class, CennsHeir.class, CloudgoatRanger.class})
class BrigidHeroOfKinsbaileTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each attacking creature target player controls")
    void deals2DamageToEachAttackingCreature() {
        addBrigidReady(player1);
        addAttacker(player2, new CloudgoatRanger());
        addAttacker(player2, new CloudgoatRanger());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).hasSize(2);
        assertThat(battlefield).allMatch(p -> p.getMarkedDamage() == 2);
    }

    @Test
    @DisplayName("Deals 2 damage to each blocking creature target player controls")
    void deals2DamageToEachBlockingCreature() {
        addBrigidReady(player1);
        Permanent blocker = addBlockingCreature(player2, new CloudgoatRanger());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Kills 2-toughness attacking creatures")
    void killsTwoToughnessAttackers() {
        addBrigidReady(player1);
        Permanent attacker = addAttacker(player2, new AvianChangeling());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(attacker.getCard());
    }

    @Test
    @DisplayName("Does not damage non-attacking or blocking creatures of target player")
    void doesNotDamageNonCombatCreatures() {
        addBrigidReady(player1);
        Permanent nonCombatCreature = addCreatureReady(player2, new CennsHeir());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(nonCombatCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage attacking creatures controlled by other players")
    void doesNotDamageOtherPlayersAttackers() {
        addBrigidReady(player1);
        Permanent ownAttacker = addAttacker(player1, new CennsHeir());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(ownAttacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Activating ability taps Brigid")
    void activatingTapsBrigid() {
        Permanent brigid = addBrigidReady(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, player2.getId());

        assertThat(brigid.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target yourself even when you have no attacking or blocking creatures")
    void canTargetSelfWithNoEffect() {
        addBrigidReady(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a player")
    void rejectsPermanentTarget() {
        Permanent brigid = addBrigidReady(player1);
        Permanent creature = addCreatureReady(player2, new AvianChangeling());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(brigid.isTapped()).isFalse();
    }

    private Permanent addBrigidReady(Player player) {
        return addCreatureReady(player, new BrigidHeroOfKinsbaile());
    }

    private Permanent addAttacker(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.setAttacking(true);
        return perm;
    }

    private Permanent addBlockingCreature(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.setBlocking(true);
        return perm;
    }
}
