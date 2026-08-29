package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VernadiShieldmateTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Vernadi Shieldmate untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent shieldmate = addCreatureReady(player1, new VernadiShieldmate());

        declareAttackers(List.of(0));

        assertThat(shieldmate.isTapped()).isFalse();
    }
}
