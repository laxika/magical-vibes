package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TickingGnomesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Ticking Gnomes deals 1 damage to a player")
    void dealsDamageToPlayer() {
        harness.addToBattlefield(player1, new TickingGnomes());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Ticking Gnomes");
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Ticking Gnomes");
    }

    @Test
    @DisplayName("The activated ability can target a creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player1, new TickingGnomes());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, findPermanent(player2, "Llanowar Elves").getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Declining echo sacrifices Ticking Gnomes at its next upkeep")
    void decliningEchoSacrificesGnomes() {
        castAndResolveGnomes();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Ticking Gnomes");
        harness.assertInGraveyard(player1, "Ticking Gnomes");
    }

    @Test
    @DisplayName("Paying echo keeps Ticking Gnomes and echo does not trigger again")
    void payingEchoKeepsGnomesAndIsOneShot() {
        castAndResolveGnomes();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Ticking Gnomes");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Ticking Gnomes");
    }

    private void castAndResolveGnomes() {
        harness.setHand(player1, List.of(new TickingGnomes()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
