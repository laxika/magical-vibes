package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChariotOfVictoryTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has first strike, trample, and haste")
    void equippedCreatureHasKeywords() {
        Permanent chariot = addChariot(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        chariot.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature loses the keywords when the Equipment is unattached")
    void keywordsLostWhenUnattached() {
        Permanent chariot = addChariot(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        chariot.setAttachedTo(creature.getId());

        chariot.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Only the equipped creature gains the keywords")
    void onlyEquippedCreatureGainsKeywords() {
        Permanent chariot = addChariot(player1);
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        chariot.setAttachedTo(equipped.getId());

        assertThat(gqs.hasKeyword(gd, equipped, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, equipped, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, equipped, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Equip ability attaches Chariot of Victory to a creature")
    void equipAbilityAttachesToCreature() {
        Permanent chariot = addChariot(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(chariot.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addChariot(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new ChariotOfVictory());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
