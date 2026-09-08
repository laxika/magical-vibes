package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GorgonsHeadTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has deathtouch")
    void equippedCreatureHasDeathtouch() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addHeadAttached(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creatures do not have deathtouch from Gorgon's Head")
    void unequippedCreaturesDoNotHaveDeathtouch() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addHeadReady(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Creature loses deathtouch when Gorgon's Head is removed")
    void creatureLosesDeathtouchWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent head = addHeadAttached(player1, creature);

        gd.playerBattlefields.get(player1.getId()).remove(head);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Resolving equip attaches Gorgon's Head and grants deathtouch")
    void resolvingEquipAttachesAndGrantsDeathtouch() {
        Permanent head = addHeadReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(head.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    private Permanent addHeadReady(Player player) {
        Permanent permanent = new Permanent(new GorgonsHead());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addHeadAttached(Player player, Permanent creature) {
        Permanent head = addHeadReady(player);
        head.setAttachedTo(creature.getId());
        return head;
    }
}
