package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MalcolmTheEyes.class, Shock.class})
class MalcolmTheEyesTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell each turn creates a Clue")
    void secondSpellCreatesClue() {
        addCreatureReady(player1, new MalcolmTheEyes());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).isEmpty();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }
}
