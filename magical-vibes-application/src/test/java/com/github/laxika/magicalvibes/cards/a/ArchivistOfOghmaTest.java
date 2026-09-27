package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchivistOfOghma.class, DiabolicTutor.class, GrizzlyBears.class})
class ArchivistOfOghmaTest extends BaseCardTest {

    @Test
    @DisplayName("When an opponent searches their library, you gain life and draw a card")
    void triggersWhenOpponentSearchesLibrary() {
        harness.addToBattlefield(player2, new ArchivistOfOghma());
        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(21);
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .isInstanceOf(GrizzlyBears.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when you search your own library")
    void doesNotTriggerWhenControllerSearchesOwnLibrary() {
        harness.addToBattlefield(player1, new ArchivistOfOghma());
        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .isInstanceOf(GrizzlyBears.class);
        assertThat(gd.stack).isEmpty();
    }
}
