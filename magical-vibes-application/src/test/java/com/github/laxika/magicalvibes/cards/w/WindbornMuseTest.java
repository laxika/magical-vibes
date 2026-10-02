package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({WindbornMuse.class, GrizzlyBears.class})
class WindbornMuseTest extends BaseCardTest {

    @Test
    @DisplayName("Windborn Muse's attack tax disappears when it loses all abilities")
    void attackTaxDisappearsWhenMuseLosesAllAbilities() {
        Permanent muse = harness.addToBattlefieldAndReturn(player2, new WindbornMuse());
        muse.setLosesAllAbilitiesUntilEndOfTurn(true);
        addCreatureReady(player1, new GrizzlyBears());

        assertThatCode(() -> declareAttackers(List.of(0))).doesNotThrowAnyException();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }
}
