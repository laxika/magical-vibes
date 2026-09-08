package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CentaurNurturer.class})
class CentaurNurturerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains 3 life")
    void entersAndGainsThreeLife() {
        harness.setLife(player1, 17);
        harness.setHand(player1, List.of(new CentaurNurturer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Tap ability adds mana of the chosen color")
    void tapsForAnyColor() {
        Permanent nurturer = addCreatureReady(player1, new CentaurNurturer());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(nurturer.isTapped()).isTrue();
    }
}
