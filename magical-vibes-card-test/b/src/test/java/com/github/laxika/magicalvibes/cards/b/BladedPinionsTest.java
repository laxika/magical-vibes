package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.g.GlintHawk;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BladedPinions.class, CarapaceForger.class, GlintHawk.class})
class BladedPinionsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Bladed Pinions and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new BladedPinions()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bladed Pinions")
                        && !p.isAttached());
    }

    @Test
    @DisplayName("Resolving equip ability attaches Bladed Pinions to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent pinions = addPinionsReady(player1);
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(pinions.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature has flying")
    void equippedCreatureHasFlying() {
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        Permanent pinions = addPinionsReady(player1);
        pinions.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature has first strike")
    void equippedCreatureHasFirstStrike() {
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        Permanent pinions = addPinionsReady(player1);
        pinions.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses flying and first strike when Bladed Pinions is removed")
    void creatureLosesKeywordsWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        Permanent pinions = addPinionsReady(player1);
        pinions.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(pinions);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Bladed Pinions does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        Permanent otherCreature = addCreatureReady(player1, new CarapaceForger());
        Permanent pinions = addPinionsReady(player1);
        pinions.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Bladed Pinions can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent pinions = addPinionsReady(player1);
        Permanent creature1 = addCreatureReady(player1, new CarapaceForger());
        Permanent creature2 = addCreatureReady(player1, new CarapaceForger());

        pinions.setAttachedTo(creature1.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FIRST_STRIKE)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(pinions.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 loses keywords
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FIRST_STRIKE)).isFalse();
        // creature2 gains keywords
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature with first strike kills blocker before regular damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // 2/2 with Bladed Pinions (flying, first strike) attacks
        Permanent attacker = addCreatureReady(player1, new CarapaceForger());
        Permanent pinions = addPinionsReady(player1);
        pinions.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        // Blocked by a 2/2 flyer without first strike
        Permanent blocker = addCreatureReady(player2, new GlintHawk());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // First strike 2/2 deals 2 damage to blocker first, killing it
        // Blocker (2 toughness) is destroyed before dealing regular damage
        // Attacker survives since blocker is dead before regular damage step
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
    }

    @Test
    void equipRequiresTwoMana() {
        Permanent pinions = addPinionsReady(player1);
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pinions.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent pinions = addPinionsReady(player1);
        Permanent creature = addCreatureReady(player2, new CarapaceForger());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pinions.isAttached()).isFalse();
    }

    @Test
    void cannotEquipNoncreature() {
        Permanent pinions = addPinionsReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, pinions.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pinions.isAttached()).isFalse();
    }

    @Test
    void cannotEquipDuringCombat() {
        Permanent pinions = addPinionsReady(player1);
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pinions.isAttached()).isFalse();
    }

    @Test
    void failedReequipLeavesOriginalCreatureEquipped() {
        Permanent pinions = addPinionsReady(player1);
        Permanent original = addCreatureReady(player1, new CarapaceForger());
        Permanent target = addCreatureReady(player1, new CarapaceForger());
        pinions.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(pinions.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.hasKeyword(gd, original, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, original, Keyword.FIRST_STRIKE)).isTrue();
    }

    private Permanent addPinionsReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new BladedPinions());
    }
}
