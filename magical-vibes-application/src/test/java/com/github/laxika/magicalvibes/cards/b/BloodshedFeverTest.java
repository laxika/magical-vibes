package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SickleRipper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodshedFever.class, SickleRipper.class, Mountain.class})
class BloodshedFeverTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Bloodshed Fever attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player1, new SickleRipper());
        harness.setHand(player1, List.of(new BloodshedFever()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bloodshed Fever")
                        && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature must attack each combat if able")
    void enchantedCreatureMustAttackWhenAble() {
        Permanent creature = addCreatureReady(player1, new SickleRipper());
        Permanent fever = harness.addToBattlefieldAndReturn(player1, new BloodshedFever());
        fever.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Summoning-sick enchanted creature is not forced to attack")
    void summoningSickCreatureIsNotForcedToAttack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SickleRipper());
        Permanent fever = harness.addToBattlefieldAndReturn(player1, new BloodshedFever());
        fever.setAttachedTo(creature.getId());

        declareAttackers(List.of());

        assertThat(creature.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Bloodshed Fever")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new BloodshedFever()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Tapped enchanted creature is not forced to attack")
    void tappedCreatureIsNotForcedToAttack() {
        Permanent creature = addCreatureReady(player1, new SickleRipper());
        creature.setTapped(true);
        Permanent fever = harness.addToBattlefieldAndReturn(player1, new BloodshedFever());
        fever.setAttachedTo(creature.getId());

        declareAttackers(List.of());

        assertThat(creature.isAttacking()).isFalse();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's enchanted creature must attack on its controller's turn")
    void opponentsCreatureMustAttack() {
        Permanent creature = addCreatureReady(player2, new SickleRipper());
        harness.setHand(player1, List.of(new BloodshedFever()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Bloodshed Fever").getAttachedTo()).isEqualTo(creature.getId());
        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        declareAttackers(player2, List.of(0));

        assertThat(creature.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The attack requirement ends when Bloodshed Fever leaves the battlefield")
    void attackRequirementEndsWhenAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new SickleRipper());
        Permanent fever = harness.addToBattlefieldAndReturn(player1, new BloodshedFever());
        fever.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(fever);
        gd.playerGraveyards.get(player1.getId()).add(fever.getCard());

        declareAttackers(List.of());

        assertThat(creature.isAttacking()).isFalse();
    }
}
