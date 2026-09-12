package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChampionsHelm.class, GrizzlyBears.class, IsamaruHoundOfKonda.class})
class ChampionsHelmTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Equipped legendary creature has hexproof")
    void equippedLegendaryCreatureHasHexproof() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Bonuses are lost when Champion's Helm becomes unattached")
    void bonusesAreLostWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());

        helm.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Equip {1} attaches Champion's Helm to a creature you control")
    void equipAttachesToCreature() {
        Permanent helm = addHelmReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addHelmReady(Player player) {
        Permanent perm = new Permanent(new ChampionsHelm());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
