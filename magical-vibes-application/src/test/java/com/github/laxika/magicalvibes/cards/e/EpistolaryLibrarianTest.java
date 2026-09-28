package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EpistolaryLibrarian.class, GrizzlyBears.class, LlanowarElves.class})
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
}
