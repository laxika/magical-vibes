package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JadelightRangerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB explores twice and puts both revealed lands into hand")
    void exploresTwiceWithLands() {
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(secondLand);
        gd.playerDecks.get(player1.getId()).addFirst(firstLand);

        castJadelightRanger();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(firstLand.getId(), secondLand.getId());
        assertThat(findJadelightRanger().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB explores twice and adds a counter for each revealed nonland")
    void exploresTwiceWithNonlands() {
        Card firstNonland = new GrizzlyBears();
        Card secondNonland = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(secondNonland);
        gd.playerDecks.get(player1.getId()).addFirst(firstNonland);

        castJadelightRanger();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findJadelightRanger().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(firstNonland.getId(), secondNonland.getId());
    }

    private void castJadelightRanger() {
        harness.setHand(player1, List.of(new JadelightRanger()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findJadelightRanger() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Jadelight Ranger"))
                .findFirst()
                .orElseThrow();
    }
}
