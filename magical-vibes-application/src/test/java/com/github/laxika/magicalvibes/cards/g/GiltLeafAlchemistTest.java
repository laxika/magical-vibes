package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiltLeafAlchemist.class, LlanowarElves.class, Forest.class})
class GiltLeafAlchemistTest extends BaseCardTest {

    @Test
    void conjuresForestWithTwoElfCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new LlanowarElves()));
        Permanent alchemist = addCreatureReady(player1, new GiltLeafAlchemist());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(alchemist.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithFewerThanTwoElfCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        addCreatureReady(player1, new GiltLeafAlchemist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
