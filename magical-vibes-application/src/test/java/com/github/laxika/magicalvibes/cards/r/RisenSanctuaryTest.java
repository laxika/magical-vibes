package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RisenSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance: attacking does not tap Risen Sanctuary")
    void vigilanceDoesNotTapWhenAttacking() {
        Permanent sanctuary = addCreatureReady(player1, new RisenSanctuary());

        declareAttackers(List.of(0));

        assertThat(sanctuary.isTapped()).isFalse();
    }
}
