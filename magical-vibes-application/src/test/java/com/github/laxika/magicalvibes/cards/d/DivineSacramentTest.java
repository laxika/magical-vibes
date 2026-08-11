package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DivineSacramentTest extends BaseCardTest {

    @Test
    void boostsWhiteCreaturesControlledByEitherPlayer() {
        harness.addToBattlefield(player1, new DivineSacrament());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertStats(player1, "Serra Angel", 5, 5);
        assertStats(player2, "Serra Angel", 5, 5);
        assertStats(player1, "Grizzly Bears", 2, 2);
        assertStats(player2, "Grizzly Bears", 2, 2);
    }

    @Test
    void thresholdAddsAnAdditionalBoostUsingTheControllersGraveyard() {
        harness.addToBattlefield(player1, new DivineSacrament());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new SerraAngel());

        fillGraveyard(player2, 7);
        assertStats(player1, "Serra Angel", 5, 5);
        assertStats(player2, "Serra Angel", 5, 5);

        fillGraveyard(player1, 7);
        assertStats(player1, "Serra Angel", 6, 6);
        assertStats(player2, "Serra Angel", 6, 6);
    }

    @Test
    void thresholdBoostDisappearsBelowSevenCards() {
        harness.addToBattlefield(player1, new DivineSacrament());
        harness.addToBattlefield(player1, new SerraAngel());
        fillGraveyard(player1, 7);

        assertStats(player1, "Serra Angel", 6, 6);

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertStats(player1, "Serra Angel", 5, 5);
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Spellbook());
        }
        harness.setGraveyard(player, cards);
    }

    private void assertStats(Player player, String cardName, int power, int toughness) {
        Permanent creature = findPermanent(player, cardName);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(toughness);
    }
}
