package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BloodServitor.class)
class BloodServitorTest extends BaseCardTest {

    @Test
    @DisplayName("When Blood Servitor enters, one Blood token is created")
    void etbCreatesOneBloodToken() {
        harness.setHand(player1, List.of(new BloodServitor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> bloods = findPermanents(player1, "Blood");
        assertThat(bloods).hasSize(1);
    }
}
