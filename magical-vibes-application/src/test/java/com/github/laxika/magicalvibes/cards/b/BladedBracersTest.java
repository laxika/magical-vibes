package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BladedBracersTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping attaches the Equipment and gives the creature +1/+1")
    void equipAttachesAndBoosts() {
        addBracersReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        Permanent bracers = findPermanent(player1, "Bladed Bracers");
        assertThat(bracers.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("A non-Human, non-Angel equipped creature does not gain vigilance")
    void nonHumanNonAngelDoesNotGainVigilance() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addBracersReady(player1).setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("An equipped Human gains vigilance")
    void equippedHumanGainsVigilance() {
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        addBracersReady(player1).setAttachedTo(human.getId());

        assertThat(gqs.hasKeyword(gd, human, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("An equipped Angel keeps vigilance and gets the boost")
    void equippedAngelGainsVigilance() {
        Permanent angel = addCreatureReady(player1, new SerraAngel());
        addBracersReady(player1).setAttachedTo(angel.getId());

        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost and vigilance go away when the Equipment leaves the battlefield")
    void effectsEndWhenEquipmentLeaves() {
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        Permanent bracers = addBracersReady(player1);
        bracers.setAttachedTo(human.getId());

        assertThat(gqs.hasKeyword(gd, human, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(bracers);

        assertThat(gqs.hasKeyword(gd, human, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
    }

    @Test
    @DisplayName("Vigilance follows the Equipment when it is moved off a Human")
    void reequipMovesVigilance() {
        Permanent bracers = addBracersReady(player1);
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bracers.setAttachedTo(human.getId());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bracers.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.hasKeyword(gd, human, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
    }

    private Permanent addBracersReady(Player player) {
        Permanent perm = new Permanent(new BladedBracers());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
