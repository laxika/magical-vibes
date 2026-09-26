package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({CageOfHands.class, Forest.class, LanternKami.class})
class CageOfHandsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Cage of Hands attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player2, new LanternKami());

        harness.setHand(player1, List.of(new CageOfHands()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Cage of Hands")
                        && p.isAttached()
                        && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature cannot attack or block")
    void enchantedCreatureCannotAttackOrBlock() {
        Permanent attacker = addCreatureReady(player1, new LanternKami());

        Permanent blocker = addCreatureReady(player2, new LanternKami());

        Permanent attackLock = harness.addToBattlefieldAndReturn(player2, new CageOfHands());
        attackLock.setAttachedTo(attacker.getId());

        Permanent blockLock = harness.addToBattlefieldAndReturn(player1, new CageOfHands());
        blockLock.setAttachedTo(blocker.getId());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Activated ability returns Cage of Hands to its owner's hand")
    void activatedAbilityReturnsSelfToHand() {
        Permanent creature = addCreatureReady(player2, new LanternKami());
        CageOfHands cage = new CageOfHands();

        harness.setHand(player1, List.of(cage));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        var battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent aura = findPermanent(player1, "Cage of Hands");
        int auraIndex = battlefield.indexOf(aura);
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, auraIndex, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cage of Hands");
        harness.assertInHand(player1, "Cage of Hands");
    }

    @Test
    @DisplayName("Cage of Hands cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new CageOfHands()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cage of Hands goes to its owner's graveyard if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player2, new LanternKami());
        CageOfHands cage = new CageOfHands();

        harness.setHand(player1, List.of(cage));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cage);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == cage);
    }
}
