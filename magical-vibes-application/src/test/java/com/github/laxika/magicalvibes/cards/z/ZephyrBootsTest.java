package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

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
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent boots = addBootsReady(player1);
        boots.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addBootsReady(Player player) {
        Permanent boots = new Permanent(new ZephyrBoots());
        boots.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(boots);
        return boots;
    }
}
