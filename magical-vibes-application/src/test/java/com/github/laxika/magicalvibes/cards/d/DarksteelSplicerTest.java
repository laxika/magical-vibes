package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarksteelSplicer.class, CrawlingChorus.class, GrizzlyBears.class})
class DarksteelSplicerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and creates one Phyrexian Golem per opponent")
    void entersAndCreatesGolemPerOpponent() {
        castDarksteelSplicer();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(golemCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Another nontoken Phyrexian entering creates a Golem")
    void anotherNontokenPhyrexianCreatesGolem() {
        harness.addToBattlefield(player1, new DarksteelSplicer());
        harness.setHand(player1, List.of(new CrawlingChorus()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(golemCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Phyrexian creature does not create a Golem")
    void nonPhyrexianDoesNotCreateGolem() {
        harness.addToBattlefield(player1, new DarksteelSplicer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(golemCount()).isZero();
    }

    @Test
    @DisplayName("Golems you control have indestructible")
    void golemsHaveIndestructible() {
        castDarksteelSplicer();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Phyrexian Golem"), Keyword.INDESTRUCTIBLE))
                .isTrue();
    }

    private void castDarksteelSplicer() {
        harness.setHand(player1, List.of(new DarksteelSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
    }

    private long golemCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .count();
    }
}
