package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderSuit.class, GrizzlyBears.class})
class SpiderSuitTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {3} attaches Spider-Suit to a creature you control")
    void equipAttachesToCreature() {
        Permanent suit = addSuitReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +2/+2 and becomes a Spider Hero")
    void equippedCreatureGetsBoostAndSubtypes() {
        Permanent creature = addCreatureReady(player1);
        Permanent suit = addSuitReady(player1);
        suit.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature))
                .contains(CardSubtype.SPIDER, CardSubtype.HERO);
    }

    @Test
    @DisplayName("Spider-Suit's bonuses disappear when it is unattached")
    void bonusesDisappearWhenUnattached() {
        Permanent creature = addCreatureReady(player1);
        Permanent suit = addSuitReady(player1);
        suit.setAttachedTo(creature.getId());
        suit.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature))
                .doesNotContain(CardSubtype.SPIDER, CardSubtype.HERO);
    }

    private Permanent addSuitReady(Player player) {
        Permanent permanent = new Permanent(new SpiderSuit());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
