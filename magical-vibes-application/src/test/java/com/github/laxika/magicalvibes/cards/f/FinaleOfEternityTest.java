package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FinaleOfEternity.class, GrizzlyBears.class, HillGiant.class, Plains.class})
class FinaleOfEternityTest extends BaseCardTest {

    @Test
    void destroysUpToThreeCreaturesWithToughnessAtMostX() {
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new FinaleOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 2, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void rejectsCreatureWithToughnessGreaterThanX() {
        var giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new FinaleOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(giant.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toughness X or less");
    }

    @Test
    void withXTenReturnsAllCreatureCardsFromControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant(), new Plains()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new FinaleOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 13);

        harness.castSorcery(player1, 0, 10);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player1, "Hill Giant")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Finale of Eternity");
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
    }
}
