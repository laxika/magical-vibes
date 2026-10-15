package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GenesisStorm.class, Forest.class, GrizzlyBears.class})
class GenesisStormTest extends BaseCardTest {

    @Test
    void revealsUntilNonlandPermanentAndPutsItOntoBattlefield() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears));

        cast();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void copiesItselfForEachCommanderCastFromCommandZone() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        gd.commanderTaxByCardId.put(commander.getId(), 4);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        cast();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void cast() {
        harness.castFromHand(player1, new GenesisStorm(), "{4}{G}{G}");
        resolveAllTriggers();
    }
}
