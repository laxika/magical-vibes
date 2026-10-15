package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantOctopus;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CosmicCube.class, Forest.class, GiantOctopus.class, GrizzlyBears.class,
        LlanowarElves.class, Mountain.class, Ornithopter.class, Naturalize.class})
class CosmicCubeTest extends BaseCardTest {

    @Test
    void looksAtSixCardsAndUsesGreatestAttackingPowerAsManaValueLimit() {
        Card eligibleAtLimit = new GrizzlyBears();
        Card tooExpensive = new GiantOctopus();
        Card eligibleBelowLimit = new LlanowarElves();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card forestAgain = new Forest();
        Card leftOnTop = new Mountain();
        setUpCubeWithAttackers();
        harness.setLibrary(player1, List.of(eligibleAtLimit, tooExpensive, eligibleBelowLimit,
                forest, mountain, forestAgain, leftOnTop));

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(eligibleAtLimit, eligibleBelowLimit);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(leftOnTop);
    }

    @Test
    void chosenSpellIsCastForFreeAndTheRestGoToTheBottom() {
        Card chosenSpell = new GrizzlyBears();
        Card secondSpell = new LlanowarElves();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card forestAgain = new Forest();
        Card mountainAgain = new Mountain();
        setUpCubeWithOneAttacker();
        harness.setLibrary(player1, List.of(chosenSpell, secondSpell, forest, mountain,
                forestAgain, mountainAgain));

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.CREATURE_SPELL
                && entry.getCard() == chosenSpell);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(secondSpell, forest, mountain, forestAgain, mountainAgain);
    }

    @Test
    void noEligibleSpellStillPutsAllLookedAtCardsOnTheBottom() {
        Card tooExpensive = new GiantOctopus();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card forestAgain = new Forest();
        Card mountainAgain = new Mountain();
        Card lastForest = new Forest();
        setUpCubeWithOneAttacker();
        harness.setLibrary(player1, List.of(tooExpensive, forest, mountain, forestAgain,
                mountainAgain, lastForest));

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                tooExpensive, forest, mountain, forestAgain, mountainAgain, lastForest);
    }

    @Test
    void mayDeclineCastingAndLeavesUnlookedCardsAboveTheRest() {
        Card spell = new GrizzlyBears();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card forestAgain = new Forest();
        Card mountainAgain = new Mountain();
        Card lastForest = new Forest();
        Card unlooked = new LlanowarElves();
        setUpCubeWithOneAttacker();
        harness.setLibrary(player1, List.of(spell, forest, mountain, forestAgain,
                mountainAgain, lastForest, unlooked));

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unlooked);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7)).containsExactlyInAnyOrder(
                spell, forest, mountain, forestAgain, mountainAgain, lastForest);
    }

    @Test
    void usesPowerAtResolutionAndIgnoresNonattackingCreatures() {
        Card spell = new GrizzlyBears();
        harness.addToBattlefield(player1, new CosmicCube());
        var attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GiantOctopus());
        harness.setLibrary(player1, List.of(spell));

        declareAttackers(List.of(1));
        attacker.setPowerModifier(-1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spell);
    }

    @Test
    void negativeGreatestAttackingPowerDoesNotAllowZeroManaValueSpells() {
        Card spell = new Ornithopter();
        harness.addToBattlefield(player1, new CosmicCube());
        var attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setPowerModifier(-3);
        harness.setLibrary(player1, List.of(spell));

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spell);
    }

    @Test
    void wardCountersOpponentsSpellWhenTwoManaIsNotPaid() {
        var cube = harness.addToBattlefieldAndReturn(player1, new CosmicCube());
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castInstant(player2, 0, cube.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cube);
        harness.assertInGraveyard(player2, "Naturalize");
    }

    @Test
    void wardAllowsOpponentsSpellWhenTwoManaIsPaid() {
        var cube = harness.addToBattlefieldAndReturn(player1, new CosmicCube());
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castInstant(player2, 0, cube.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cosmic Cube");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
    private void setUpCubeWithAttackers() {
        harness.addToBattlefield(player1, new CosmicCube());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new LlanowarElves());
    }

    private void setUpCubeWithOneAttacker() {
        harness.addToBattlefield(player1, new CosmicCube());
        addCreatureReady(player1, new GrizzlyBears());
    }
}
