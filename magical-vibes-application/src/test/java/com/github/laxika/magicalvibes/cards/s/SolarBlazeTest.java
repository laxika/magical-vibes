package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.Justice;
import com.github.laxika.magicalvibes.cards.w.WallOfSwords;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SolarBlaze.class, GiantSpider.class, HillGiant.class, Justice.class, WallOfSwords.class})
class SolarBlazeTest extends BaseCardTest {

    @Test
    @DisplayName("Each creature deals damage to itself equal to its power")
    void eachCreatureDealsItsPowerToItself() {
        harness.addToBattlefield(player1, new WallOfSwords());
        harness.addToBattlefield(player2, new GiantSpider());
        Permanent wall = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent spider = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new SolarBlaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isEqualTo(3);
        assertThat(spider.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Wall of Swords");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Each creature is the source of its own damage")
    void eachCreatureIsItsOwnDamageSource() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new Justice());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SolarBlaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }
}
