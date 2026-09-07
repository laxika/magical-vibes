package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FencersMagemark.class, Pacifism.class, GrizzlyBears.class})
class FencersMagemarkTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts and grants first strike to each enchanted creature you control")
    void boostsAndGrantsFirstStrikeToEnchantedCreaturesYouControl() {
        Permanent firstBears = addCreature(player1);
        Permanent secondBears = addCreature(player1);
        Permanent unenchantedBears = addCreature(player1);
        Permanent opponentBears = addCreature(player2);
        attach(new FencersMagemark(), firstBears, player1);
        attach(new Pacifism(), secondBears, player1);

        assertThat(gqs.getEffectivePower(gd, firstBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstBears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, firstBears, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, secondBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondBears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, secondBears, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, unenchantedBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unenchantedBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, unenchantedBears, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Stops affecting enchanted creatures when Fencer's Magemark leaves the battlefield")
    void stopsAffectingCreaturesWhenRemoved() {
        Permanent bears = addCreature(player1);
        Permanent magemark = attach(new FencersMagemark(), bears, player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(magemark);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addCreature(Player controller) {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(controller.getId()).add(creature);
        return creature;
    }

    private Permanent attach(Card auraCard, Permanent creature, Player controller) {
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
