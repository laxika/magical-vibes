package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.t.TheFirstDoctor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YidrisMaelstromWielder.class, GrizzlyBears.class, LlanowarElves.class, TheFirstDoctor.class})
class YidrisMaelstromWielderTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage grants cascade to spells cast from hand for the rest of the turn")
    void combatDamageGrantsCascadeToHandSpells() {
        LlanowarElves cascadeHit = new LlanowarElves();
        harness.setLibrary(player1, List.of(cascadeHit));
        Permanent yidris = addCreatureReady(player1, new YidrisMaelstromWielder());
        yidris.setAttacking(true);

        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        resolveAllTriggers();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Llanowar Elves");
    }

    @Test
    void cascadeHitIsCastForFreeWithoutGainingAnotherCascadeFromExile() {
        grantCascade();
        LlanowarElves hit = new LlanowarElves();
        harness.setLibrary(player1, List.of(hit));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void cascadeMayBeDeclinedAndAppliesToLaterHandSpellsToo() {
        grantCascade();
        LlanowarElves hit = new LlanowarElves();
        harness.setLibrary(player1, List.of(hit));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(hit);
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    @Test
    void cascadeContinuesAfterYidrisLeavesTheBattlefield() {
        Permanent yidris = grantCascade();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, yidris));
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    @Test
    void spellsDoNotCascadeBeforeCombatDamage() {
        harness.addToBattlefield(player1, new YidrisMaelstromWielder());
        LlanowarElves untouched = new LlanowarElves();
        harness.setLibrary(player1, List.of(untouched));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentsHandSpellsDoNotGainCascade() {
        grantCascade();
        LlanowarElves untouched = new LlanowarElves();
        harness.setLibrary(player2, List.of(untouched));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({YidrisMaelstromWielder.class, GrizzlyBears.class, TheFirstDoctor.class})
    void grantedCascadeTriggersTheFirstDoctor() {
        Permanent yidris = grantCascade();
        harness.addToBattlefield(player1, new TheFirstDoctor());
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, yidris.getId());
        resolveAllTriggers();
        assertThat(yidris.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    private Permanent grantCascade() {
        Permanent yidris = addCreatureReady(player1, new YidrisMaelstromWielder());
        yidris.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        return yidris;
    }
}
