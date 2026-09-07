package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AboshansDesire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GateHound.class, AboshansDesire.class, GrizzlyBears.class})
class GateHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Gate Hound grants no vigilance while it is not enchanted")
    void doesNotGrantVigilanceWhileNotEnchanted() {
        Permanent hound = addCreature(player1, new GateHound());
        Permanent otherCreature = addCreature(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreature(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, hound, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Gate Hound gives your creatures vigilance while it is enchanted")
    void grantsVigilanceWhileEnchanted() {
        Permanent hound = addCreature(player1, new GateHound());
        Permanent otherCreature = addCreature(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreature(player2, new GrizzlyBears());
        Permanent aura = attachAura(player2, hound);

        assertThat(gqs.hasKeyword(gd, hound, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();

        gd.playerBattlefields.get(player2.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, hound, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.VIGILANCE)).isFalse();
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new AboshansDesire());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
