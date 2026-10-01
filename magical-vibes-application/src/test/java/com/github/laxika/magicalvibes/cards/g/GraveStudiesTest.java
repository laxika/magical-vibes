package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.TeachersPest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@DisplayName("Grave Studies")
@CardUsed({GraveStudies.class, TeachersPest.class, GrizzlyBears.class})
class GraveStudiesTest extends BaseCardTest {

    @Test
    @DisplayName("Conjures Teacher's Pest, then each player sacrifices a creature")
    void conjuresPestThenEachPlayerSacrifices() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GraveStudies()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Teacher's Pest");
        harness.assertNotOnBattlefield(player1, "Teacher's Pest");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
