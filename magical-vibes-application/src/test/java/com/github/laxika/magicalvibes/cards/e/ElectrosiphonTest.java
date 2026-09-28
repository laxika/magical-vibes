package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Electrosiphon.class, CounselOfTheSoratami.class})
class ElectrosiphonTest extends BaseCardTest {

    @Test
    @DisplayName("Counters target spell and grants energy equal to its mana value")
    void countersSpellAndGrantsEnergyEqualToManaValue() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Electrosiphon()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(3);
        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        harness.assertInGraveyard(player2, "Electrosiphon");
    }
}
