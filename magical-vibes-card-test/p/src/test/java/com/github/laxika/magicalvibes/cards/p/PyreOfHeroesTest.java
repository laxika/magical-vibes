package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.i.InvokeTheDivine;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PyreOfHeroes.class, LlanowarElves.class, ElvishVisionary.class, GrizzlyBears.class,
        HillGiant.class, MaskwoodNexus.class, InvokeTheDivine.class})
class PyreOfHeroesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and finds a same-type creature with mana value one higher")
    void sacrificesCreatureAndSearchesForMatchingCreature() {
        harness.addToBattlefield(player1, new PyreOfHeroes());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(
                new ElvishVisionary(),
                new GrizzlyBears(),
                new HillGiant()));

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(1)
                .allMatch(card -> card.getName().equals("Elvish Visionary"));

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Elvish Visionary");
    }

    @Test
    @DisplayName("Does not find a creature with the wrong type or mana value")
    void doesNotFindNonMatchingCreature() {
        harness.addToBattlefield(player1, new PyreOfHeroes());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    void retainsCreatureTypesFromBeforeSacrificeWhenNexusIsDestroyedInResponse() {
        harness.addToBattlefield(player1, new PyreOfHeroes());
        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new MaskwoodNexus());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new InvokeTheDivine()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.castInstant(player2, 0, nexus.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Maskwood Nexus");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(1);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        harness.addToBattlefield(player1, new PyreOfHeroes());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void cannotActivateWithoutCreatureToSacrifice() {
        harness.addToBattlefield(player1, new PyreOfHeroes());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paysManaAndTapsPyreBeforeResolution() {
        Permanent pyre = harness.addToBattlefieldAndReturn(player1, new PyreOfHeroes());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new ElvishVisionary()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(pyre.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Elvish Visionary");
    }
}
