package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CobbledWings.class, WalkingCorpse.class})
class CobbledWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Cobbled Wings and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new CobbledWings()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Cobbled Wings")
                        && !p.isAttached());
    }

    @Test
    @DisplayName("Resolving equip ability attaches Cobbled Wings to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new CobbledWings());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(wings.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(wings.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Equipped creature has flying")
    void equippedCreatureHasFlying() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new CobbledWings());
        wings.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Creature loses flying when Cobbled Wings is removed")
    void creatureLosesFlyingWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new CobbledWings());
        wings.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(wings);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cobbled Wings does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent otherCreature = addCreatureReady(player1, new WalkingCorpse());
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new CobbledWings());
        wings.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cobbled Wings can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new CobbledWings());
        Permanent creature1 = addCreatureReady(player1, new WalkingCorpse());
        Permanent creature2 = addCreatureReady(player1, new WalkingCorpse());

        wings.setAttachedTo(creature1.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FLYING)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(wings.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotEquipWithoutMana() {
        harness.addToBattlefield(player1, new CobbledWings());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefield(player1, new CobbledWings());
        Permanent creature = addCreatureReady(player2, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipNoncreature() {
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new CobbledWings());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wings.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wings.isAttached()).isFalse();
    }

    @Test
    void cannotEquipDuringUpkeep() {
        harness.addToBattlefield(player1, new CobbledWings());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipWhileStackIsNotEmpty() {
        harness.addToBattlefield(player1, new CobbledWings());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    void failedReequipLeavesEquipmentOnOriginalCreature() {
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new CobbledWings());
        Permanent original = addCreatureReady(player1, new WalkingCorpse());
        Permanent target = addCreatureReady(player1, new WalkingCorpse());
        wings.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();
        assertThat(wings.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.hasKeyword(gd, original, Keyword.FLYING)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipmentGrantsFlyingAcrossControllers() {
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new CobbledWings());
        Permanent creature = addCreatureReady(player2, new WalkingCorpse());
        wings.setAttachedTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void tappedEquipmentCanEquipTappedCreature() {
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new CobbledWings());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        wings.tap();
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(wings.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(wings.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }
}
