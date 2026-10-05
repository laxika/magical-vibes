package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.e.EsikaGodOfTheTree;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MoxAmber;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JodahTheUnifier.class, CaptainSisay.class, EsikaGodOfTheTree.class, GrizzlyBears.class, MoxAmber.class, AdelizTheCinderWind.class, HillGiant.class, IsamaruHoundOfKonda.class, LlanowarElves.class})
class JodahTheUnifierTest extends BaseCardTest {

    @Test
    @DisplayName("Legendary creatures you control get +X/+X, including Jodah")
    void boostsLegendaryCreaturesByLegendaryCreatureCount() {
        Permanent jodah = harness.addToBattlefieldAndReturn(player1, new JodahTheUnifier());
        Permanent isamaru = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, jodah)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, jodah)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, isamaru)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, isamaru)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a legendary spell from hand cascades into a legendary nonland card")
    void castsLegendarySpellFromHandAndFindsLegendaryCard() {
        prepareCasterTurn();
        harness.addToBattlefield(player1, new JodahTheUnifier());

        LlanowarElves belowHit = new LlanowarElves();
        harness.setLibrary(player1, List.of(
                new HillGiant(), new GrizzlyBears(), new IsamaruHoundOfKonda(), belowHit));

        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Isamaru, Hound of Konda");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowHit);
    }

    @Test
    @DisplayName("Nonlegendary spells do not trigger Jodah")
    void nonlegendarySpellDoesNotTrigger() {
        prepareCasterTurn();
        harness.addToBattlefield(player1, new JodahTheUnifier());

        LlanowarElves libraryCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void countsOnlyControlledLegendaryCreatures() {
        Permanent jodah = harness.addToBattlefieldAndReturn(player1, new JodahTheUnifier());
        Permanent sisay = harness.addToBattlefieldAndReturn(player1, new CaptainSisay());
        Permanent opposingLegend = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        harness.addToBattlefield(player1, new MoxAmber());
        harness.addToBattlefield(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, jodah)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, jodah)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, sisay)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sisay)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingLegend)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingLegend)).isEqualTo(2);
    }

    @Test
    void freelyCastsLegendaryArtifactFromExileWithoutRetriggering() {
        prepareCasterTurn();
        harness.addToBattlefield(player1, new JodahTheUnifier());
        Card skipped = new GrizzlyBears();
        Card found = new MoxAmber();
        Card remaining = new IsamaruHoundOfKonda();
        harness.setLibrary(player1, List.of(skipped, found, remaining));
        harness.setHand(player1, List.of(new CaptainSisay()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(entry -> entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getSourceZone()).isEqualTo(Zone.EXILE);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining, skipped);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mox Amber");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Captain Sisay");
    }

    @Test
    void legendaryZeroManaSpellReturnsEntireLibraryWhenNoLesserCardExists() {
        prepareCasterTurn();
        harness.addToBattlefield(player1, new JodahTheUnifier());
        Card legendary = new IsamaruHoundOfKonda();
        Card zeroMana = new MoxAmber();
        Card nonlegendary = new LlanowarElves();
        harness.setLibrary(player1, List.of(legendary, zeroMana, nonlegendary));
        harness.setHand(player1, List.of(new MoxAmber()));

        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(legendary, zeroMana, nonlegendary);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mox Amber");
    }

    @Test
    void emptyLibraryDoesNotPreventOriginalLegendarySpellResolving() {
        prepareCasterTurn();
        harness.addToBattlefield(player1, new JodahTheUnifier());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new IsamaruHoundOfKonda()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Isamaru, Hound of Konda");
    }

    @Test
    void mayCastHigherManaValueBackFaceOfQualifyingLegendaryCard() {
        prepareCasterTurn();
        harness.addToBattlefield(player1, new JodahTheUnifier());
        harness.setLibrary(player1, List.of(new EsikaGodOfTheTree()));
        harness.setHand(player1, List.of(new CaptainSisay()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player1, "The Prismatic Bridge");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Prismatic Bridge");
        harness.assertNotOnBattlefield(player1, "Esika, God of the Tree");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentsLegendarySpellDoesNotTrigger() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new JodahTheUnifier());
        Card libraryCard = new MoxAmber();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player2, List.of(new IsamaruHoundOfKonda()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Isamaru, Hound of Konda");
    }

    private void prepareCasterTurn() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
    }
}
