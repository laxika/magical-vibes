package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VigilantBalothTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Vigilant Baloth untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent baloth = addCreatureReady(player1, new VigilantBaloth());

        declareAttackers(List.of(0));

        assertThat(baloth.isTapped()).isFalse();
    }
}
