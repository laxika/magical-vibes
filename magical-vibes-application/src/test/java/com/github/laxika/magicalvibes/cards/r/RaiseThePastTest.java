package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RaiseThePastTest extends BaseCardTest {

    @Test
    @DisplayName("Returns every own creature card with mana value 2 or less")
    void returnsEligibleCreatureCards() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card hillGiant = new HillGiant();
        Card plains = new Plains();
        Card opponentElves = new LlanowarElves();
        Card raiseThePast = new RaiseThePast();
        harness.setGraveyard(player1, List.of(bears, elves, hillGiant, plains));
        harness.setGraveyard(player2, List.of(opponentElves));
        harness.setHand(player1, List.of(raiseThePast));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(bears, elves);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(hillGiant, plains, raiseThePast);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentElves);
    }

    @Test
    @DisplayName("Returns nothing when no creature card has mana value 2 or less")
    void noEligibleCreatureCardsReturnsNothing() {
        Card hillGiant = new HillGiant();
        Card plains = new Plains();
        Card raiseThePast = new RaiseThePast();
        harness.setGraveyard(player1, List.of(hillGiant, plains));
        harness.setHand(player1, List.of(raiseThePast));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(hillGiant, plains, raiseThePast);
    }
}
