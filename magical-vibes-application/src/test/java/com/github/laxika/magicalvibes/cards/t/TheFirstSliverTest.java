package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CloudshredderSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFirstSliver.class, GrizzlyBears.class, LlanowarElves.class,
        CloudshredderSliver.class, Ornithopter.class, UniversalAutomaton.class})
class TheFirstSliverTest extends BaseCardTest {

    @Test
    @DisplayName("The First Sliver cascades when cast")
    void cascadesWhenCast() {
        setupCasterTurn();
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        harness.setHand(player1, List.of(new TheFirstSliver()));
        addFiveColors();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }

    @Test
    @DisplayName("Sliver spells cascade while The First Sliver is on the battlefield")
    void sliverSpellCascades() {
        setupCasterTurn();
        harness.addToBattlefield(player1, new TheFirstSliver());
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        harness.setHand(player1, List.of(new CloudshredderSliver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Llanowar Elves");
    }

    @Test
    @DisplayName("Non-Sliver spells do not cascade from The First Sliver")
    void nonSliverSpellDoesNotCascade() {
        setupCasterTurn();
        harness.addToBattlefield(player1, new TheFirstSliver());
        LlanowarElves untouched = new LlanowarElves();
        harness.setLibrary(player1, List.of(untouched));

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
    }

    @Test
    void changelingSpellGetsCascade() {
        setupCasterTurn();
        harness.addToBattlefield(player1, new TheFirstSliver());
        Ornithopter hit = new Ornithopter();
        harness.setLibrary(player1, List.of(hit));
        harness.setHand(player1, List.of(new UniversalAutomaton()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
    }

    @Test
    void sliverHitDoesNotCascadeWhileFirstSliverIsStillOnStack() {
        setupCasterTurn();
        LlanowarElves untouched = new LlanowarElves();
        harness.setLibrary(player1, List.of(new CloudshredderSliver(), untouched));
        harness.setHand(player1, List.of(new TheFirstSliver()));
        addFiveColors();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cloudshredder Sliver");
        harness.assertNotOnBattlefield(player1, "The First Sliver");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "The First Sliver");
    }

    @Test
    void sliverCastFromCascadeGetsAnotherCascade() {
        setupCasterTurn();
        harness.addToBattlefield(player1, new TheFirstSliver());
        Ornithopter hit = new Ornithopter();
        harness.setLibrary(player1, List.of(new CloudshredderSliver(), hit));
        harness.setHand(player1, List.of(new TheFirstSliver()));
        addFiveColors();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
    }

    @Test
    void opponentsFirstSliverDoesNotGrantCascade() {
        setupCasterTurn();
        harness.addToBattlefield(player2, new TheFirstSliver());
        LlanowarElves untouched = new LlanowarElves();
        harness.setLibrary(player1, List.of(untouched));
        harness.setHand(player1, List.of(new CloudshredderSliver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cloudshredder Sliver");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
    }

    @Test
    void decliningCascadeReturnsHitAndLetsFirstSliverResolve() {
        setupCasterTurn();
        LlanowarElves hit = new LlanowarElves();
        harness.setLibrary(player1, List.of(hit));
        harness.setHand(player1, List.of(new TheFirstSliver()));
        addFiveColors();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The First Sliver");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(hit);
        assertThat(gd.stack).isEmpty();
    }

    private void setupCasterTurn() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
    }

    private void addFiveColors() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
