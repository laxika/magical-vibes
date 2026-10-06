package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

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

    @Test
    void decliningDemonstrateCreatesNoCopies() {
        harness.addToBattlefield(player1, new SilverquillLecturer());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(findPermanent(player1, "Grizzly Bears").getCard().isToken()).isFalse();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void demonstratedCreatureCopiesResolveAsTokensWithoutBeingCast() {
        harness.addToBattlefield(player1, new SilverquillLecturer());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2)
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1)
                .allMatch(p -> p.getCard().isToken());
    }

    @Test
    void opponentsCreatureSpellsDoNotGainDemonstrate() {
        harness.addToBattlefield(player1, new SilverquillLecturer());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void lecturerDoesNotGrantDemonstrateToItsOwnCast() {
        harness.castFromHand(player1, new SilverquillLecturer(), "{4}{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Silverquill Lecturer")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Silverquill Lecturer");
    }

    @Test
    void twoLecturersGrantTwoIndependentDemonstrateAbilities() {
        harness.addToBattlefield(player1, new SilverquillLecturer());
        harness.addToBattlefield(player1, new SilverquillLecturer());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(3);
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(2);
    }
}
