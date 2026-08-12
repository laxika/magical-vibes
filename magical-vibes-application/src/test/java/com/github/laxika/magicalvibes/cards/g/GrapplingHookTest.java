package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrapplingHookTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has double strike")
    void equippedCreatureHasDoubleStrike() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hook = addEquipment(player1);
        hook.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Attacking lets the controller have a target creature block the equipped creature")
    void attackTriggerCanRequireTargetCreatureToBlock() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hook = addEquipment(player1);
        hook.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blocker.getMustBlockIds()).containsExactly(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the attack trigger imposes no block requirement")
    void decliningAttackTriggerDoesNothing() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hook = addEquipment(player1);
        hook.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(blocker.getMustBlockIds()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addEquipment(com.github.laxika.magicalvibes.model.Player player) {
        Permanent equipment = new Permanent(new GrapplingHook());
        gd.playerBattlefields.get(player.getId()).add(equipment);
        return equipment;
    }
}
