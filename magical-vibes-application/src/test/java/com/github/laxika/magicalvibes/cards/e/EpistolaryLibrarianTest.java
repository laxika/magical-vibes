package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EpistolaryLibrarian.class, GrizzlyBears.class, LlanowarElves.class, SwordsToPlowshares.class})
class EpistolaryLibrarianTest extends BaseCardTest {

    @Test
    void oneAttackerCannotCastSpellAboveOneManaValue() {
        GrizzlyBears handSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(handSpell));
        addCreatureReady(player1, new EpistolaryLibrarian());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handSpell);
    }

    @Test
    void numberOfAttackersSetsFreeCastManaValueLimit() {
        GrizzlyBears handSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(handSpell));
        addCreatureReady(player1, new EpistolaryLibrarian());
        addCreatureReady(player1, new LlanowarElves());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.CREATURE_SPELL
                && entry.getCard() == handSpell);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(handSpell);
    }

    @Test
    void mayDeclineFreeCast() {
        LlanowarElves handSpell = new LlanowarElves();
        harness.setHand(player1, List.of(handSpell));
        addCreatureReady(player1, new EpistolaryLibrarian());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handSpell);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == handSpell);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void canCastOnlyOneSpellFromEachAttackTrigger() {
        LlanowarElves firstSpell = new LlanowarElves();
        LlanowarElves secondSpell = new LlanowarElves();
        harness.setHand(player1, List.of(firstSpell, secondSpell));
        addCreatureReady(player1, new EpistolaryLibrarian());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.CREATURE_SPELL)
                .hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void removedAttackerReducesManaValueLimitAtResolution() {
        GrizzlyBears handSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(handSpell));
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        addCreatureReady(player1, new EpistolaryLibrarian());
        Permanent otherAttacker = addCreatureReady(player1, new LlanowarElves());

        declareAttackers(List.of(0, 1));
        harness.castAndResolveInstant(player2, 0, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(otherAttacker.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handSpell);
    }

    @Test
    void removingOnlyAttackerLeavesZeroManaValueLimit() {
        LlanowarElves handSpell = new LlanowarElves();
        harness.setHand(player1, List.of(handSpell));
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        Permanent librarian = addCreatureReady(player1, new EpistolaryLibrarian());

        declareAttackers(List.of(0));
        harness.castAndResolveInstant(player2, 0, librarian.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(librarian.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handSpell);
    }
}
