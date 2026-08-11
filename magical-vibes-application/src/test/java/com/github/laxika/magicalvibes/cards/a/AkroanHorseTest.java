package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AkroanHorseTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives Akroan Horse to an opponent")
    void etbGivesControlToOpponent() {
        harness.setHand(player1, List.of(new AkroanHorse()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Akroan Horse");
        harness.assertOnBattlefield(player2, "Akroan Horse");
    }

    @Test
    @DisplayName("At its controller's upkeep each opponent creates a 1/1 white Soldier")
    void eachOpponentCreatesSoldier() {
        harness.addToBattlefield(player1, new AkroanHorse());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        List<Permanent> soldiers = findPermanents(player2, "Soldier");
        assertThat(soldiers).hasSize(1);
        assertThat(soldiers.getFirst().getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldiers.getFirst().getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
    }
}
