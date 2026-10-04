package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.model.Keyword;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fireshrieker.class, AlphaMyr.class})
class FireshriekerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Fireshrieker and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new Fireshrieker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Fireshrieker")
                        && !p.isAttached());
    }

    @Test
    @DisplayName("Resolving equip attaches Fireshrieker to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent fireshrieker = addFireshriekerReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(fireshrieker.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature has double strike")
    void equippedCreatureHasDoubleStrike() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent fireshrieker = addFireshriekerReady(player1);
        fireshrieker.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses double strike when Fireshrieker is removed")
    void creatureLosesDoubleStrikeWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent fireshrieker = addFireshriekerReady(player1);
        fireshrieker.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(fireshrieker);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Fireshrieker does not grant double strike to unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent otherCreature = addCreatureReady(player1, new AlphaMyr());
        Permanent fireshrieker = addFireshriekerReady(player1);
        fireshrieker.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Fireshrieker can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent fireshrieker = addFireshriekerReady(player1);
        Permanent creature1 = addCreatureReady(player1, new AlphaMyr());
        Permanent creature2 = addCreatureReady(player1, new AlphaMyr());

        fireshrieker.setAttachedTo(creature1.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(fireshrieker.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equip fizzles if target creature is removed before resolution")
    void equipFizzlesIfTargetRemoved() {
        addFireshriekerReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Alpha Myr"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent remaining = findPermanent(player1, "Fireshrieker");
        assertThat(remaining.getAttachedTo()).isNull();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Cannot equip during an opponent's turn")
    void cannotEquipDuringOpponentsTurn() {
        addFireshriekerReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot equip a creature controlled by an opponent")
    void cannotEquipOpponentsCreature() {
        addFireshriekerReady(player1);
        Permanent creature = addCreatureReady(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot equip a noncreature permanent")
    void cannotEquipNoncreaturePermanent() {
        Permanent fireshrieker = addFireshriekerReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, fireshrieker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Equip pays two generic mana")
    void equipPaysTwoGenericMana() {
        Permanent fireshrieker = addFireshriekerReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(fireshrieker.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped unblocked creature deals both first-strike and regular combat damage")
    void equippedAttackerDealsDamageTwice() {
        Permanent attacker = addCreatureReady(player1, new AlphaMyr());
        Permanent fireshrieker = addFireshriekerReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, attacker.getId());
        harness.passBothPriorities();
        assertThat(fireshrieker.getAttachedTo()).isEqualTo(attacker.getId());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Killing a blocker with first-strike damage does not let regular damage through")
    void killedBlockerDoesNotLetDamageThrough() {
        Permanent attacker = addCreatureReady(player1, new AlphaMyr());
        Permanent fireshrieker = addFireshriekerReady(player1);
        fireshrieker.setAttachedTo(attacker.getId());
        harness.addToBattlefield(player2, new AlphaMyr());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Alpha Myr");
        harness.assertInGraveyard(player2, "Alpha Myr");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An equipped blocker kills an attacker before regular combat damage")
    void equippedBlockerDealsFirstStrikeDamage() {
        addCreatureReady(player1, new AlphaMyr());
        Permanent blocker = addCreatureReady(player2, new AlphaMyr());
        Permanent fireshrieker = addFireshriekerReady(player2);
        fireshrieker.setAttachedTo(blocker.getId());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Alpha Myr");
        harness.assertOnBattlefield(player2, "Alpha Myr");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Equip cannot be activated while another equip ability is on the stack")
    void cannotEquipWithNonemptyStack() {
        Permanent fireshrieker = addFireshriekerReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);

        harness.passBothPriorities();
        assertThat(fireshrieker.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Failed re-equip leaves Fireshrieker attached to its original creature")
    void failedReEquipPreservesOriginalAttachment() {
        Permanent fireshrieker = addFireshriekerReady(player1);
        Permanent original = addCreatureReady(player1, new AlphaMyr());
        Permanent target = addCreatureReady(player1, new AlphaMyr());
        fireshrieker.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(fireshrieker.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.hasKeyword(gd, original, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip does not grant double strike if Fireshrieker leaves before resolution")
    void equipmentRemovedBeforeEquipResolves() {
        Permanent fireshrieker = addFireshriekerReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(fireshrieker);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addFireshriekerReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Fireshrieker());
        perm.setSummoningSick(false);
        return perm;
    }
}
