package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SloppityBilepiper.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class, Forest.class})
class SloppityBilepiperTest extends BaseCardTest {

    @Test
    void nextCreatureSpellHasCascade() {
        setupBilepiper();
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        activateBilepiper(sacrificed);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    void cascadeAppliesOnlyToTheNextCreatureSpell() {
        setupBilepiper();
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new LlanowarElves()));

        activateBilepiper(sacrificed);
        harness.passBothPriorities();

        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canSacrificeItselfAndStillGrantCascade() {
        setupBilepiper();
        Permanent bilepiper = findPermanent(player1, "Sloppity Bilepiper");
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        activateBilepiper(bilepiper);
        harness.assertInGraveyard(player1, "Sloppity Bilepiper");
        harness.assertNotOnBattlefield(player1, "Sloppity Bilepiper");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void multipleGrantsCreateSeparateCascadeTriggers() {
        setupBilepiper();
        Permanent secondBilepiper = addCreatureReady(player1, new SloppityBilepiper());
        Permanent firstSacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondSacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new LlanowarElves(), new LlanowarElves()));

        activateBilepiper(firstSacrifice);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(secondBilepiper), null, null);
        harness.handlePermanentChosen(player1, secondSacrifice.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY))
                .hasSize(2);
    }

    @Test
    void unusedGrantExpiresAtEndOfTurn() {
        setupBilepiper();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        activateBilepiper(sacrificed);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    private void setupBilepiper() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        addCreatureReady(player1, new SloppityBilepiper());
    }

    private void activateBilepiper(Permanent sacrificed) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
    }
}
