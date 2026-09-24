package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NayaSoulbeast.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class NayaSoulbeastTest extends BaseCardTest {

    @Test
    @DisplayName("Cast trigger reveals both top cards and uses their total mana value for counters")
    void entersWithCountersEqualToRevealedManaValues() {
        Card ownTopCard = new GrizzlyBears();
        Card opponentTopCard = new AirElemental();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opponentTopCard));
        harness.setHand(player1, List.of(new NayaSoulbeast()));
        addManaToCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(ownTopCard);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(opponentTopCard);

        harness.passBothPriorities();

        Permanent soulbeast = findPermanent(player1, "Naya Soulbeast");
        assertThat(soulbeast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, soulbeast)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, soulbeast)).isEqualTo(7);
    }

    @Test
    @DisplayName("An empty library contributes no counters and a zero-power Soulbeast dies")
    void emptyLibrariesContributeZero() {
        Card ownTopCard = new Forest();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new NayaSoulbeast()));
        addManaToCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(ownTopCard);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Naya Soulbeast");
        harness.assertInGraveyard(player1, "Naya Soulbeast");
    }

    private void addManaToCast() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
