package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuatlisSnubhorn.class})
class HuatlisSnubhornTest extends BaseCardTest {

    @Test
    void attackingDoesNotTapSnubhorn() {
        Permanent snubhorn = addCreatureReady(player1, new HuatlisSnubhorn());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(snubhorn.isAttacking()).isTrue();
        assertThat(snubhorn.isTapped()).isFalse();
    }
}
