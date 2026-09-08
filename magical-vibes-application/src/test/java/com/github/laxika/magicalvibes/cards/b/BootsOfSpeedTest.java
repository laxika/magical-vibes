package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BootsOfSpeed.class, GrizzlyBears.class})
class BootsOfSpeedTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping Boots of Speed gives the creature +1/+0 and haste")
    void equippingGivesBoostAndHaste() {
        Permanent boots = addBootsReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(boots.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creatures do not get the Boots of Speed bonus")
    void unequippedCreatureDoesNotGetBonus() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addBootsReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The creature loses the Boots of Speed bonus when the Equipment leaves")
    void creatureLosesBonusWhenBootsLeave() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent boots = addBootsReady(player1);
        boots.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(boots);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    private Permanent addBootsReady(Player player) {
        Permanent boots = new Permanent(new BootsOfSpeed());
        boots.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(boots);
        return boots;
    }
}
