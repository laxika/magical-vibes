package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZephyrBoots.class, Forest.class, GrizzlyBears.class, SerraAngel.class})
class ZephyrBootsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping Zephyr Boots gives the creature flying")
    void equippingGivesFlying() {
        Permanent boots = addBootsReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(boots.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creature does not get flying from Zephyr Boots")
    void unequippedCreatureDoesNotGetFlying() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addBootsReady(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Equipped creature's combat damage draws a card, then discards a card")
    void combatDamageDrawsThenDiscards() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent boots = addBootsReady(player1);
        boots.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Zephyr Boots do not trigger when equipped creature deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        harness.setHand(player1, List.of(new GrizzlyBears()));

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent boots = addBootsReady(player1);
        boots.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void canDiscardTheCardJustDrawn() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addBootsReady(player1).setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.assertInHand(player1, "Forest");
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void emptyHandStillDrawsThenDiscards() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addBootsReady(player1).setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.assertInHand(player1, "Forest");
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void bootsControllerLootsWhenOpponentsEquippedCreatureDealsDamage() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new SerraAngel()));
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addBootsReady(player1).setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.assertInHand(player1, "Forest");
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player2, "Serra Angel");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void reequippingMovesFlyingToNewCreature() {
        Permanent boots = addBootsReady(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        boots.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(boots.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
    }

    @Test
    void equipRequiresTwoMana() {
        Permanent boots = addBootsReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(boots.isAttached()).isFalse();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent boots = addBootsReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(boots.isAttached()).isFalse();
    }

    @Test
    void unequippedAttackerDoesNotTriggerBoots() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        addBootsReady(player1).setAttachedTo(equipped.getId());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void equipCannotTargetNoncreature() {
        Permanent boots = addBootsReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, boots.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(boots.isAttached()).isFalse();
    }

    @Test
    void cannotEquipWithAnAbilityOnTheStack() {
        Permanent boots = addBootsReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(boots.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void cannotEquipDuringUpkeep() {
        Permanent boots = addBootsReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(boots.isAttached()).isFalse();
    }

    private Permanent addBootsReady(Player player) {
        return addCreatureReady(player, new ZephyrBoots());
    }
}
