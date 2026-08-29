package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BasiliskCollarTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has deathtouch and lifelink")
    void equippedCreatureHasKeywords() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent collar = addBasiliskCollarReady(player1);
        collar.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Creature loses deathtouch and lifelink when Basilisk Collar is removed")
    void creatureLosesKeywordsWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent collar = addBasiliskCollarReady(player1);
        collar.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(collar);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Equip {2} attaches Basilisk Collar to a creature you control")
    void equipsToControlledCreature() {
        Permanent collar = addBasiliskCollarReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(collar.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Basilisk Collar does not grant keywords to an unequipped creature")
    void doesNotAffectUnequippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addBasiliskCollarReady(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    private Permanent addBasiliskCollarReady(Player player) {
        Permanent perm = new Permanent(new BasiliskCollar());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
