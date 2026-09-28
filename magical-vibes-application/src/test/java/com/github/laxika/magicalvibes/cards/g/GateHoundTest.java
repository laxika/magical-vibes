package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FaithsFetters;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GateHound.class, FaithsFetters.class, GrayscaledGharial.class})
class GateHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Gate Hound grants no vigilance while it is not enchanted")
    void doesNotGrantVigilanceWhileNotEnchanted() {
        Permanent hound = addCreatureReady(player1, new GateHound());
        Permanent otherCreature = addCreatureReady(player1, new GrayscaledGharial());
        Permanent opponentCreature = addCreatureReady(player2, new GrayscaledGharial());

        assertThat(gqs.hasKeyword(gd, hound, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Gate Hound gives your creatures vigilance while it is enchanted")
    void grantsVigilanceWhileEnchanted() {
        Permanent hound = addCreatureReady(player1, new GateHound());
        Permanent otherCreature = addCreatureReady(player1, new GrayscaledGharial());
        Permanent opponentCreature = addCreatureReady(player2, new GrayscaledGharial());
        Permanent aura = attachAura(player2, hound);

        assertThat(gqs.hasKeyword(gd, hound, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();

        gd.playerBattlefields.get(player2.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, hound, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.VIGILANCE)).isFalse();
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new FaithsFetters());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
