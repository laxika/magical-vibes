package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KarganDragonriderTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying when its controller controls a Dragon")
    void hasFlyingWithDragon() {
        harness.addToBattlefield(player1, new KarganDragonrider());
        harness.addToBattlefield(player1, createDragon());

        Permanent dragonrider = findPermanent(player1, "Kargan Dragonrider");
        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not have flying without a Dragon")
    void noFlyingWithoutDragon() {
        harness.addToBattlefield(player1, new KarganDragonrider());

        Permanent dragonrider = findPermanent(player1, "Kargan Dragonrider");
        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Dragon does not grant flying")
    void opponentDragonDoesNotCount() {
        harness.addToBattlefield(player1, new KarganDragonrider());
        harness.addToBattlefield(player2, createDragon());

        Permanent dragonrider = findPermanent(player1, "Kargan Dragonrider");
        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses flying when the Dragon leaves the battlefield")
    void losesFlyingWhenDragonLeaves() {
        harness.addToBattlefield(player1, new KarganDragonrider());
        harness.addToBattlefield(player1, createDragon());

        Permanent dragonrider = findPermanent(player1, "Kargan Dragonrider");
        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getSubtypes().contains(CardSubtype.DRAGON));

        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isFalse();
    }

    private Card createDragon() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.DRAGON));
        return card;
    }
}
