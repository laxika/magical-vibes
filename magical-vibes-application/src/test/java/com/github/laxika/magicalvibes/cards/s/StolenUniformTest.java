package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WeaponsVendor;
import com.github.laxika.magicalvibes.cards.w.WarriorsSword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StolenUniform.class, WeaponsVendor.class, WarriorsSword.class})
class StolenUniformTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of an Equipment and attaches it to your creature")
    void gainsControlAndAttachesEquipment() {
        Permanent creature = addCreatureReady(player1, new WeaponsVendor());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new WarriorsSword());

        castAndResolveStolenUniform(creature, equipment);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Unattaches the Equipment when its temporary control ends")
    void unattachesEquipmentWhenControlEnds() {
        Permanent creature = addCreatureReady(player1, new WeaponsVendor());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new WarriorsSword());

        castAndResolveStolenUniform(creature, equipment);

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
    }

    @Test
    @DisplayName("Still gains control when the chosen creature is illegal on resolution")
    void gainsControlIfCreatureLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new WeaponsVendor());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new WarriorsSword());

        castStolenUniform(creature, equipment);
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Does not trigger when control ends while attached to an opponent's creature")
    void doesNotTriggerWhenAttachedCreatureIsControlledByOpponent() {
        Permanent creature = addCreatureReady(player1, new WeaponsVendor());
        Permanent opponentCreature = addCreatureReady(player2, new WeaponsVendor());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new WarriorsSword());
        equipment.setAttachedTo(opponentCreature.getId());
        castStolenUniform(creature, equipment);
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(opponentCreature.getId());

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(opponentCreature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipment already controlled by the caster stays attached after cleanup")
    void ownEquipmentStaysAttachedAfterCleanup() {
        Permanent creature = addCreatureReady(player1, new WeaponsVendor());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new WarriorsSword());
        castAndResolveStolenUniform(creature, equipment);

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Moves Equipment away from the creature it was previously attached to")
    void movesEquipmentFromPreviousCreature() {
        Permanent creature = addCreatureReady(player1, new WeaponsVendor());
        Permanent previousCreature = addCreatureReady(player2, new WeaponsVendor());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new WarriorsSword());
        equipment.setAttachedTo(previousCreature.getId());

        castAndResolveStolenUniform(creature, equipment);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Does not attach an Equipment that leaves before resolution")
    void doesNothingIfEquipmentLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new WeaponsVendor());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new WarriorsSword());
        castStolenUniform(creature, equipment);
        gd.playerBattlefields.get(player2.getId()).remove(equipment);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(equipment);
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when control ends while the Equipment is unattached")
    void doesNotTriggerWhenEquipmentIsUnattached() {
        Permanent creature = addCreatureReady(player1, new WeaponsVendor());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new WarriorsSword());
        castStolenUniform(creature, equipment);
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolveStolenUniform(Permanent creature, Permanent equipment) {
        harness.setHand(player1, List.of(new StolenUniform()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), equipment.getId()));
    }

    private void castStolenUniform(Permanent creature, Permanent equipment) {
        harness.setHand(player1, List.of(new StolenUniform()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(creature.getId(), equipment.getId()));
    }
}
