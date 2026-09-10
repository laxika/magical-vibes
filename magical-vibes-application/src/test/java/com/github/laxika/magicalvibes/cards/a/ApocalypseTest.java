package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JackalPup;
import com.github.laxika.magicalvibes.cards.j.JinxedIdol;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shocker;
import com.github.laxika.magicalvibes.cards.w.Warmth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Apocalypse.class, JackalPup.class, Island.class, JinxedIdol.class, Mountain.class,
        Shocker.class, Warmth.class})
class ApocalypseTest extends BaseCardTest {

    private void addCost() {
        // {2}{R}{R}{R}
        harness.addMana(player1, ManaColor.RED, 5);
    }

    @Test
    @DisplayName("Exiles every permanent on both battlefields, lands included")
    void exilesAllPermanents() {
        harness.addToBattlefield(player1, new JackalPup());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new JinxedIdol());
        harness.addToBattlefield(player1, new Warmth());
        harness.addToBattlefield(player2, new JackalPup());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new Apocalypse()));
        addCost();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Jackal Pup", "Island", "Jinxed Idol", "Warmth");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Jackal Pup", "Mountain");
    }

    @Test
    @DisplayName("Caster discards their hand; opponent's hand is untouched")
    void discardsOnlyCastersHand() {
        harness.setHand(player1, List.of(new Apocalypse(), new Shocker(), new JackalPup()));
        harness.setHand(player2, List.of(new Mountain(), new Island()));
        addCost();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Mountain", "Island");
        harness.assertInGraveyard(player1, "Shocker");
        harness.assertInGraveyard(player1, "Jackal Pup");
    }

    @Test
    @DisplayName("Resolves with an empty hand and empty board")
    void resolvesWithNothingToHit() {
        harness.setHand(player1, List.of(new Apocalypse()));
        addCost();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Apocalypse");
    }
}
