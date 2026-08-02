package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HotSoupTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip attaches Hot Soup to the target creature")
    void equipAttachesToCreature() {
        Permanent soup = addHotSoupReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(soup.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature can't be blocked")
    void equippedCreatureCantBeBlocked() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent soup = addHotSoupReady(player1);
        soup.setAttachedTo(creature.getId());

        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creatures can still be blocked")
    void unequippedCreatureCanBeBlocked() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent soup = addHotSoupReady(player1);
        soup.setAttachedTo(creature.getId());

        assertThat(gqs.hasCantBeBlocked(gd, other)).isFalse();
    }

    @Test
    @DisplayName("Non-lethal damage to the equipped creature destroys it")
    void damageDestroysEquippedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears()); // 2/2 survives 1 damage
        Permanent soup = addHotSoupReady(player1);
        soup.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hot Soup");
        assertThat(soup.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Damage to another creature does not destroy the equipped creature")
    void damageToOtherCreatureDoesNotTrigger() {
        Permanent equipped = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        Permanent soup = addHotSoupReady(player1);
        soup.setAttachedTo(equipped.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, other.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(equipped.getId()));
        assertThat(soup.getAttachedTo()).isEqualTo(equipped.getId());
    }

    private Permanent addHotSoupReady(Player player) {
        Permanent perm = new Permanent(new HotSoup());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
