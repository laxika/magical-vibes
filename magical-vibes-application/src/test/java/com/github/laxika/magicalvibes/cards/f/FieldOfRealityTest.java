package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FieldOfReality.class, Forest.class, IsamaruHoundOfKonda.class, LanternKami.class})
class FieldOfRealityTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Field of Reality attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());

        harness.setHand(player1, List.of(new FieldOfReality()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Field of Reality")
                        && p.isAttached()
                        && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Spirit cannot block the enchanted creature")
    void spiritCannotBlockEnchantedCreature() {
        Permanent attacker = addCreatureReady(player1, new IsamaruHoundOfKonda());
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FieldOfReality());
        aura.setAttachedTo(attacker.getId());

        Permanent blocker = addCreatureReady(player2, new LanternKami());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-Spirit creature can block the enchanted creature")
    void nonSpiritCanBlockEnchantedCreature() {
        Permanent attacker = addCreatureReady(player1, new IsamaruHoundOfKonda());
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FieldOfReality());
        aura.setAttachedTo(attacker.getId());

        Permanent blocker = addCreatureReady(player2, new IsamaruHoundOfKonda());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Field of Reality only restricts the creature it enchants")
    void onlyEnchantedCreatureCannotBeBlockedBySpirits() {
        Permanent enchantedCreature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        enchantedCreature.setAttacking(true);
        Permanent otherAttacker = addCreatureReady(player1, new IsamaruHoundOfKonda());
        otherAttacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FieldOfReality());
        aura.setAttachedTo(enchantedCreature.getId());

        Permanent blocker = addCreatureReady(player2, new LanternKami());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int otherAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(otherAttacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, otherAttackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Activated ability returns Field of Reality to its owner's hand")
    void activatedAbilityReturnsSelfToHand() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());

        FieldOfReality auraCard = new FieldOfReality();
        harness.setHand(player1, List.of(auraCard));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Field of Reality");
        int auraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(aura);
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, auraIndex, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Field of Reality");
        harness.assertInHand(player1, "Field of Reality");
    }

    @Test
    @DisplayName("Field of Reality cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new FieldOfReality()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Field of Reality goes to its owner's graveyard if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player2, new IsamaruHoundOfKonda());
        FieldOfReality aura = new FieldOfReality();

        harness.setHand(player1, List.of(aura));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Field of Reality");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == aura);
    }
}
