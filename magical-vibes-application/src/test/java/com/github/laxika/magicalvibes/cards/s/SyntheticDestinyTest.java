package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyntheticDestiny.class, AirElemental.class, GrizzlyBears.class,
        LlanowarElves.class, LightningBolt.class})
class SyntheticDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles your creatures immediately and replaces them at the next end step")
    void exilesAndReplacesCreaturesAtNextEndStep() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        prepareSpell(List.of(new LightningBolt(), new AirElemental(),
                new LightningBolt(), new GrizzlyBears()));

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears", "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Lightning Bolt");
    }

    @Test
    @DisplayName("Snapshots the number of creatures exiled")
    void snapshotsExiledCreatureCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareSpell(List.of(new AirElemental(), new LlanowarElves()));

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new LlanowarElves());

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Shuffles all revealed noncreature cards when no creature card is found")
    void noCreatureCardsAreFound() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareSpell(List.of(new LightningBolt(), new LightningBolt()));

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Lightning Bolt", "Lightning Bolt");
    }

    private void prepareSpell(List<Card> libraryCards) {
        harness.setHand(player1, List.of(new SyntheticDestiny()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        GameData gameData = harness.getGameData();
        gameData.playerDecks.get(player1.getId()).clear();
        gameData.playerDecks.get(player1.getId()).addAll(libraryCards);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
