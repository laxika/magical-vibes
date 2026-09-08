package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(UnhallowedPhalanx.class)
class UnhallowedPhalanxTest extends BaseCardTest {

    @Test
    @DisplayName("Unhallowed Phalanx enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new UnhallowedPhalanx()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent phalanx = findPermanent(player1, "Unhallowed Phalanx");
        assertThat(phalanx.isTapped()).isTrue();
    }
}
