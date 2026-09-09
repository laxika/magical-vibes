package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GerrardsWisdom;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarrowGhoul.class, BenalishInfantry.class, GerrardsWisdom.class})
class BarrowGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling the top creature card of your graveyard keeps Barrow Ghoul")
    void payingExilesTopCreatureCard() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Barrow Ghoul");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Benalish Infantry");
    }

    @Test
    @DisplayName("Declining the exile sacrifices Barrow Ghoul")
    void decliningSacrifices() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Barrow Ghoul");
        harness.assertInGraveyard(player1, "Benalish Infantry");
    }

    @Test
    @DisplayName("With no creature card in the graveyard the Ghoul is sacrificed without a prompt")
    void noCreatureCardSacrifices() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new GerrardsWisdom()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Barrow Ghoul");
        harness.assertInGraveyard(player1, "Gerrard's Wisdom");
    }

    @Test
    @DisplayName("Noncreature cards above the top creature card are skipped, not blockers")
    void skipsNoncreatureCardsAboveIt() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new BenalishInfantry(), new GerrardsWisdom()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Barrow Ghoul");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Gerrard's Wisdom");
    }

    @Test
    @DisplayName("Only one of two Ghouls can exile the same eligible creature card")
    void oneCreatureCardPaysForOnlyOneGhoul() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Barrow Ghoul")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Barrow Ghoul");
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Benalish Infantry");
    }

    @Test
    @DisplayName("Can still exile a creature after the Ghoul leaves the battlefield")
    void canPayAfterSourceLeavesBattlefield() {
        var ghoul = harness.addToBattlefieldAndReturn(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, ghoul));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Barrow Ghoul");
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Benalish Infantry");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void noTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Barrow Ghoul");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
