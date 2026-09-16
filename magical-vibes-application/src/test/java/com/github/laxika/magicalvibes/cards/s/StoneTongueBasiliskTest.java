package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MetamorphicWurm;
import com.github.laxika.magicalvibes.cards.m.Mortivore;
import com.github.laxika.magicalvibes.cards.m.MysticZealot;
import com.github.laxika.magicalvibes.cards.p.PsionicGift;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoneTongueBasilisk.class, MetamorphicWurm.class, Mortivore.class,
        MysticZealot.class, PsionicGift.class})
class StoneTongueBasiliskTest extends BaseCardTest {

    @Test
    @DisplayName("Threshold makes all able creatures block Stone-Tongue Basilisk")
    void thresholdForcesAllAbleCreaturesToBlock() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        Permanent basilisk = addAttackingCreature(player1, new StoneTongueBasilisk());
        Permanent firstBlocker = addCreatureReady(player2, new MetamorphicWurm());
        Permanent secondBlocker = addCreatureReady(player2, new MetamorphicWurm());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(basilisk.isAttacking()).isTrue();
        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Threshold does not force blocks below seven cards in the controller's graveyard")
    void thresholdDoesNotForceBlocksBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        addAttackingCreature(player1, new StoneTongueBasilisk());
        Permanent blocker = addCreatureReady(player2, new MetamorphicWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Threshold forces every able blocker but not a tapped creature")
    void thresholdOnlyForcesAbleBlockers() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        addAttackingCreature(player1, new StoneTongueBasilisk());
        Permanent readyBlocker = addCreatureReady(player2, new MetamorphicWurm());
        Permanent tappedBlocker = addCreatureReady(player2, new MetamorphicWurm());
        tappedBlocker.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(readyBlocker.isBlocking()).isTrue();
        assertThat(tappedBlocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Combat damage schedules the damaged creature for destruction at end of combat")
    void combatDamageDestroysCreatureAtEndOfCombat() {
        harness.setGraveyard(player1, graveyardWithCards(5));
        Permanent basilisk = addAttackingCreature(player1, new StoneTongueBasilisk());
        Permanent mortivore = addCreatureReady(player2, new Mortivore());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(basilisk);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(mortivore);

        resolveAllTriggers();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(mortivore);
    }

    @Test
    @DisplayName("The trigger does not apply when another creature deals the combat damage")
    void triggerIsSelfScoped() {
        Permanent basilisk = addCreatureReady(player1, new StoneTongueBasilisk());
        Permanent attacker = addAttackingCreature(player1, new MysticZealot());
        Permanent blocker = addCreatureReady(player2, new MetamorphicWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(basilisk);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);

        resolveAllTriggers();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker, basilisk);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Noncombat damage does not trigger Stone-Tongue Basilisk's destruction ability")
    void noncombatDamageDoesNotTrigger() {
        Permanent basilisk = addCreatureReady(player1, new StoneTongueBasilisk());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PsionicGift());
        aura.setAttachedTo(basilisk.getId());
        Permanent target = addCreatureReady(player2, new MetamorphicWurm());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(true);
        return permanent;
    }

    private List<Card> graveyardWithCards(int count) {
        List<Card> graveyard = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            graveyard.add(new StoneTongueBasilisk());
        }
        return graveyard;
    }
}
