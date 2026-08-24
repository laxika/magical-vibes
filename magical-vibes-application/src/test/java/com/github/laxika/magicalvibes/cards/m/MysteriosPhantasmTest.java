package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysteriosPhantasm.class, GrizzlyBears.class})
class MysteriosPhantasmTest extends BaseCardTest {

    @Test
    void attackingMillsOneCard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent phantasm = addCreatureReady(player1, new MysteriosPhantasm());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(phantasm)));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }
}
