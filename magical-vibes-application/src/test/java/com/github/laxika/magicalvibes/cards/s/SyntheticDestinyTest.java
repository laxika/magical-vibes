package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyntheticDestiny.class, AirElemental.class, GrizzlyBears.class,
        LlanowarElves.class, LightningBolt.class, PsychogenicProbe.class})
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

    @Test
    @DisplayName("Creates the delayed trigger even when no creatures are exiled")
    void noCreaturesStillCreatesDelayedTrigger() {
        prepareSpell(List.of(new AirElemental(), new LightningBolt()));
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(SyntheticDestiny.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Puts all remaining creature cards onto the battlefield when fewer remain than were exiled")
    void fewerCreatureCardsThanExiledCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        prepareSpell(List.of(new LightningBolt(), new AirElemental(), new LightningBolt()));
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Lightning Bolt", "Lightning Bolt");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Uses the next opponent end step and leaves opposing creatures alone")
    void resolvesAtOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        prepareSpell(List.of(new AirElemental(), new GrizzlyBears()));
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Still shuffles when no creatures were exiled")
    void noCreaturesStillShufflesLibrary() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        prepareSpell(List.of(new AirElemental(), new LightningBolt()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep();
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Still shuffles an empty library at the next end step")
    void emptyLibraryStillShuffles() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        prepareSpell(List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep();
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Shuffles even when every revealed card is a creature")
    void noNoncreaturesRevealedStillShuffles() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        prepareSpell(List.of(new AirElemental(), new LlanowarElves()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep();
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 2);
        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Llanowar Elves");
    }

    private void prepareSpell(List<Card> libraryCards) {
        harness.setHand(player1, List.of(new SyntheticDestiny()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setLibrary(player1, libraryCards);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
