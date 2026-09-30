package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilverquillLecturer.class, GrizzlyBears.class, Shock.class})
class SilverquillLecturerTest extends BaseCardTest {

    @Test
    void creatureSpellHasDemonstrate() {
        harness.addToBattlefield(player1, new SilverquillLecturer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2)
                .extracting(StackEntry::getControllerId)
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());
    }

    @Test
    void nonCreatureSpellDoesNotHaveDemonstrate() {
        harness.addToBattlefield(player1, new SilverquillLecturer());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNull();
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
    }
}
