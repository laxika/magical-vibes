package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
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
        harness.handleMayAbilityChosen(player1, true);

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
        gd.recordCommanderCastFromCommandZone(player1.getId());
        gd.recordCommanderCastFromCommandZone(player1.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        cast();
        for (int i = 0; i < 3; i++) {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Putting the card onto the battlefield is optional; declined, it goes to the bottom")
    void mayLeaveTheRevealedCard() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest below = new Forest();
        harness.setLibrary(player1, List.of(bears, below));

        cast();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below, bears);
    }

    private void cast() {
        harness.castFromHand(player1, new GenesisStorm(), "{4}{G}{G}");
        resolveAllTriggers();
    }
}
