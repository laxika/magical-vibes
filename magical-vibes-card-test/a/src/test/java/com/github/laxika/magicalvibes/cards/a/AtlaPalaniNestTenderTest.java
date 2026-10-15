package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CarrionFeeder;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.m.MirrorEntity;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AtlaPalaniNestTender.class, Forest.class, LlanowarElves.class, CarrionFeeder.class,
        MaskwoodNexus.class, MirrorEntity.class, PsychogenicProbe.class, WrathOfGod.class})
class AtlaPalaniNestTenderTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 0/1 green Egg creature token with defender")
    void createsEggToken() {
        Permanent atla = addCreatureReady(player1, new AtlaPalaniNestTender());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent egg = findPermanent(player1, "Egg");
        assertThat(atla.isTapped()).isTrue();
        assertThat(egg.getCard().getPower()).isZero();
        assertThat(egg.getCard().getToughness()).isEqualTo(1);
        assertThat(egg.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(egg.getCard().getSubtypes()).containsExactly(CardSubtype.EGG);
        assertThat(gqs.hasKeyword(gd, egg, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Puts a creature from the library onto the battlefield when an Egg dies")
    void eggDeathRevealsCreatureToBattlefield() {
        addCreatureReady(player1, new AtlaPalaniNestTender());
        harness.addToBattlefield(player1, new CarrionFeeder());
        Card forestInLibrary = new Forest();
        Card creatureInLibrary = new LlanowarElves();
        harness.setLibrary(player1, List.of(forestInLibrary, creatureInLibrary));
        createEgg();

        Permanent egg = findPermanent(player1, "Egg");
        harness.activateAbility(player1, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, egg.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forestInLibrary);
    }

    @Test
    @DisplayName("Does not trigger when a non-Egg creature dies")
    void nonEggDeathDoesNotTrigger() {
        addCreatureReady(player1, new AtlaPalaniNestTender());
        harness.addToBattlefield(player1, new CarrionFeeder());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Card creatureInLibrary = new LlanowarElves();
        harness.setLibrary(player1, List.of(creatureInLibrary));

        harness.activateAbility(player1, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, elf.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creatureInLibrary);
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void randomBottomOrderDoesNotCountAsShufflingLibrary() {
        addCreatureReady(player1, new AtlaPalaniNestTender());
        harness.addToBattlefield(player1, new CarrionFeeder());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new LlanowarElves()));
        createEgg();

        sacrifice(player1, 1, findPermanent(player1, "Egg"));

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertLife(player1, 20);
    }

    @Test
    void triggersForAtlaHerselfWhenSheIsAnEgg() {
        Permanent atla = addCreatureReady(player1, new AtlaPalaniNestTender());
        harness.addToBattlefield(player1, new CarrionFeeder());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        sacrifice(player1, 1, atla);

        harness.assertInGraveyard(player1, "Atla Palani, Nest Tender");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void triggersForCreatureThatGainedEggTypeUntilEndOfTurn() {
        addCreatureReady(player1, new AtlaPalaniNestTender());
        harness.addToBattlefield(player1, new CarrionFeeder());
        harness.addToBattlefield(player1, new MirrorEntity());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Card libraryCreature = new LlanowarElves();
        harness.setLibrary(player1, List.of(libraryCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, 2, null);
        harness.passBothPriorities();

        sacrifice(player1, 1, elf);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void noCreatureRevealsEntireLibraryAndReturnsAllCards() {
        addCreatureReady(player1, new AtlaPalaniNestTender());
        harness.addToBattlefield(player1, new CarrionFeeder());
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        createEgg();

        sacrifice(player1, 1, findPermanent(player1, "Egg"));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void emptyLibraryDoesNotPutAnythingOntoBattlefield() {
        addCreatureReady(player1, new AtlaPalaniNestTender());
        harness.addToBattlefield(player1, new CarrionFeeder());
        harness.setLibrary(player1, List.of());
        createEgg();

        sacrifice(player1, 1, findPermanent(player1, "Egg"));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void opposingEggDeathDoesNotTriggerAtla() {
        addCreatureReady(player1, new AtlaPalaniNestTender());
        harness.addToBattlefield(player2, new CarrionFeeder());
        harness.addToBattlefield(player2, new MaskwoodNexus());
        Permanent opposingEgg = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Card libraryCreature = new LlanowarElves();
        harness.setLibrary(player1, List.of(libraryCreature));

        sacrifice(player2, 0, opposingEgg);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCreature);
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void eachEggDeathRevealsAnotherCreature() {
        addCreatureReady(player1, new AtlaPalaniNestTender());
        harness.addToBattlefield(player1, new CarrionFeeder());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        Permanent firstEgg = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent secondEgg = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new LlanowarElves(), new LlanowarElves()));

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, firstEgg.getId());
        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, secondEgg.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Llanowar Elves")).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void triggersWhenAtlaAndEggDieSimultaneously() {
        addCreatureReady(player1, new AtlaPalaniNestTender());
        createEgg();
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Atla Palani, Nest Tender");
        harness.assertNotOnBattlefield(player1, "Egg");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void stopsAtFirstCreatureAndLeavesUnrevealedCardsInOrder() {
        addCreatureReady(player1, new AtlaPalaniNestTender());
        harness.addToBattlefield(player1, new CarrionFeeder());
        Card firstCreature = new LlanowarElves();
        Card forest = new Forest();
        Card secondCreature = new LlanowarElves();
        harness.setLibrary(player1, List.of(firstCreature, forest, secondCreature));
        createEgg();

        sacrifice(player1, 1, findPermanent(player1, "Egg"));

        assertThat(countPermanents(player1, "Llanowar Elves")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, secondCreature);
    }
    private void createEgg() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void sacrifice(Player player, int feederIndex, Permanent permanent) {
        harness.activateAbility(player, feederIndex, null, null);
        harness.handlePermanentChosen(player, permanent.getId());
        resolveAllTriggers();
    }
}
