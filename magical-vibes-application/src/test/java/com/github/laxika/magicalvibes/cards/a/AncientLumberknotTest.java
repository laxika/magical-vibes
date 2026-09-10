package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncientLumberknot.class, GiantSpider.class, GoblinPiker.class, GrizzlyBears.class})
class AncientLumberknotTest extends BaseCardTest {

    @Test
    @DisplayName("Ancient Lumberknot assigns combat damage equal to its toughness")
    void ancientLumberknotUsesItsToughness() {
        Permanent lumberknot = addCreatureReady(player1, new AncientLumberknot());

        assertThat(gqs.getEffectiveCombatDamage(gd, lumberknot)).isEqualTo(4);
    }

    @Test
    @DisplayName("A creature you control with greater toughness assigns toughness combat damage")
    void ownCreatureWithGreaterToughnessUsesToughness() {
        addCreatureReady(player1, new AncientLumberknot());
        Permanent spider = addCreatureReady(player1, new GiantSpider());

        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures without greater toughness assign power combat damage")
    void creaturesWithoutGreaterToughnessUsePower() {
        addCreatureReady(player1, new AncientLumberknot());
        Permanent piker = addCreatureReady(player1, new GoblinPiker());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(2);
        assertThat(gqs.getEffectiveCombatDamage(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's creature is not affected")
    void opponentCreatureIsNotAffected() {
        addCreatureReady(player1, new AncientLumberknot());
        Permanent opponentSpider = addCreatureReady(player2, new GiantSpider());

        assertThat(gqs.getEffectiveCombatDamage(gd, opponentSpider)).isEqualTo(2);
    }

    @Test
    @DisplayName("The effect ends when Ancient Lumberknot leaves the battlefield")
    void effectEndsWhenAncientLumberknotLeavesBattlefield() {
        Permanent lumberknot = addCreatureReady(player1, new AncientLumberknot());
        Permanent spider = addCreatureReady(player1, new GiantSpider());

        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(lumberknot);

        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(2);
    }
}
