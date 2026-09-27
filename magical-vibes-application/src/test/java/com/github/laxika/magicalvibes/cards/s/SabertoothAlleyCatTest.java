package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenevolentAncestor;
import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
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

@CardUsed({SabertoothAlleyCat.class, BorosRecruit.class, BenevolentAncestor.class})
class SabertoothAlleyCatTest extends BaseCardTest {

    @Test
    @DisplayName("Attacks each combat if able")
    void mustAttackEachCombat() {
        addCreatureReady(player1, new SabertoothAlleyCat());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Its ability prevents creatures without defender from blocking it")
    void nonDefenderCannotBlockAfterActivation() {
        Permanent cat = addCreatureReady(player1, new SabertoothAlleyCat());
        Permanent recruit = addCreatureReady(player2, new BorosRecruit());
        activateBlockingRestriction();
        cat.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(recruit, cat))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with defender");
    }

    @Test
    @DisplayName("Its ability still allows creatures with defender to block it")
    void defenderCanBlockAfterActivation() {
        Permanent cat = addCreatureReady(player1, new SabertoothAlleyCat());
        Permanent ancestor = addCreatureReady(player2, new BenevolentAncestor());
        activateBlockingRestriction();
        cat.setAttacking(true);
        prepareDeclareBlockers();

        declareBlock(ancestor, cat);

        assertThat(ancestor.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Its blocking restriction expires at end of turn")
    void nonDefenderCanBlockAfterRestrictionExpires() {
        Permanent cat = addCreatureReady(player1, new SabertoothAlleyCat());
        Permanent recruit = addCreatureReady(player2, new BorosRecruit());
        activateBlockingRestriction();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        cat.setAttacking(true);
        prepareDeclareBlockers();
        declareBlock(recruit, cat);

        assertThat(recruit.isBlocking()).isTrue();
    }

    private void activateBlockingRestriction() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
