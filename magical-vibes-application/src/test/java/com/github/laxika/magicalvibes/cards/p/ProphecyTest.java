package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProphecyTest extends BaseCardTest {

    @Test
    @DisplayName("Revealing a land on top of the opponent's library gains the caster 1 life")
    void gainsLifeWhenTopCardIsLand() {
        harness.setHand(player1, List.of(new Prophecy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(new Forest(), new Island(), new Island())));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Revealing a nonland card gains no life")
    void noLifeWhenTopCardIsNonland() {
        harness.setHand(player1, List.of(new Prophecy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        gd.playerDecks.put(player2.getId(),
                new ArrayList<>(List.of(new GrizzlyBears(), new Island(), new Island())));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("The targeted opponent's library is shuffled but keeps its size")
    void shufflesTargetLibrary() {
        harness.setHand(player1, List.of(new Prophecy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            deck.add(i % 2 == 0 ? new Forest() : new GrizzlyBears());
        }
        gd.playerDecks.put(player2.getId(), new ArrayList<>(deck));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        List<Card> after = gd.playerDecks.get(player2.getId());
        assertThat(after).hasSize(40);
        assertThat(after).containsExactlyInAnyOrderElementsOf(deck);
        assertThat(after).isNotEqualTo(deck);
    }

    @Test
    @DisplayName("Schedules a draw for the caster at the next upkeep")
    void schedulesDrawForCaster() {
        harness.setHand(player1, List.of(new Prophecy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target the caster themselves")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Prophecy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");

        assertThat(gd.stack).isEmpty();
    }
}
