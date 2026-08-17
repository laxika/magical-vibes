package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WaryOkapiTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Wary Okapi untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent okapi = addCreatureReady(player1, new WaryOkapi());

        declareAttackers(List.of(0));

        assertThat(okapi.isTapped()).isFalse();
    }
}
