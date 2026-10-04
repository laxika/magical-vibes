package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GazeOfTheGorgon.class, GoliathSpider.class, GlassGolem.class, Watchwolf.class, Forest.class})
class GazeOfTheGorgonTest extends BaseCardTest {

    @Test
    @DisplayName("Regenerates the target and destroys its combat opponents at end of combat")
    void regeneratesTargetAndDestroysCombatOpponents() {
        Permanent target = addCreatureReady(player1, new Watchwolf());
        Permanent blockerOne = addCreatureReady(player2, new GlassGolem());
        Permanent blockerTwo = addCreatureReady(player2, new GlassGolem());
        Permanent bystander = addCreatureReady(player2, new GlassGolem());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        castGaze(player1, target);
        resolveAllTriggers();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(blockerOne, blockerTwo, bystander);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blockerOne.getId(), 2, blockerTwo.getId(), 1));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bystander);
    }

    @Test
    @DisplayName("Destroys creatures that were blocked by the target at end of combat")
    void destroysCreaturesBlockedByTarget() {
        Permanent attacker = addCreatureReady(player1, new GoliathSpider());
        Permanent target = addCreatureReady(player2, new GlassGolem());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        castGaze(player1, target);
        resolveAllTriggers();

        assertThat(target.getRegenerationShield()).isEqualTo(1);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
        assertThat(target.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("A noncreature permanent cannot be targeted")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GazeOfTheGorgon()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Includes creatures that block after the spell resolves")
    void includesBlockersDeclaredAfterResolution() {
        Permanent target = addCreatureReady(player1, new Watchwolf());
        Permanent blocker = addCreatureReady(player2, new GoliathSpider());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            castGaze(player1, target);
            resolveAllTriggers();
        });
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("End-of-combat destruction waits for its delayed trigger to resolve")
    void destructionUsesTheStackAtEndOfCombat() {
        Permanent target = addCreatureReady(player1, new Watchwolf());
        Permanent blocker = addCreatureReady(player2, new GoliathSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        castGaze(player1, target);
        resolveAllTriggers();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.stack).isNotEmpty();

        castGaze(player2, blocker);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getRegenerationShield()).isZero();
        assertThat(blocker.isTapped()).isTrue();
    }

    private void castGaze(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new GazeOfTheGorgon()));
        harness.addMana(caster, ManaColor.GREEN, 4);
        harness.castInstant(caster, 0, target.getId());
    }
}
