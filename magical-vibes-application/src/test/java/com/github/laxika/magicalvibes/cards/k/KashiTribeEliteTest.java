package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SkeletalSnake;
import com.github.laxika.magicalvibes.cards.s.SosukeSonOfSeshiro;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KashiTribeElite.class, SosukeSonOfSeshiro.class, SkeletalSnake.class, Shock.class, GiantSpider.class})
class KashiTribeEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Legendary Snakes you control have shroud")
    void grantsShroudToLegendarySnakesYouControl() {
        Permanent kashi = addCreatureReady(player1, new KashiTribeElite());
        Permanent sosuke = addCreatureReady(player1, new SosukeSonOfSeshiro());
        Permanent ordinarySnake = addCreatureReady(player1, new SkeletalSnake());
        Permanent opposingSosuke = addCreatureReady(player2, new SosukeSonOfSeshiro());

        assertThat(gqs.hasKeyword(gd, sosuke, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, kashi, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, ordinarySnake, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSosuke, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud prevents targeting a legendary Snake")
    void cannotBeTargetedBySpells() {
        addCreatureReady(player1, new KashiTribeElite());
        Permanent sosuke = addCreatureReady(player1, new SosukeSonOfSeshiro());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, sosuke.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Combat damage to a creature taps it and locks its next untap step")
    void combatDamageTapsAndLocksDamagedCreature() {
        Permanent kashi = addCreatureReady(player1, new KashiTribeElite());
        kashi.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent spider = findPermanent(player2, "Giant Spider");
        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getSkipUntapCount()).isEqualTo(1);
    }
}
