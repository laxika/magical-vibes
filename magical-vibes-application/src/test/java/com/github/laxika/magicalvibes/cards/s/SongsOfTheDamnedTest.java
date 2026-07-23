package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SongsOfTheDamnedTest extends BaseCardTest {

    @Test
    @DisplayName("Adds {B} for each creature card in controller's graveyard")
    void addsBlackManaPerCreatureCardInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant(), new Mountain()));
        harness.setHand(player1, List.of(new SongsOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Two creature cards in GY at resolution (land ignored); cast cost spent the {B}
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds no mana when graveyard has no creature cards")
    void addsNoManaWithEmptyCreatureCount() {
        harness.setGraveyard(player1, List.of(new Mountain()));
        harness.setHand(player1, List.of(new SongsOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not count opponent's creature cards")
    void ignoresOpponentGraveyard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setHand(player1, List.of(new SongsOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }
}
