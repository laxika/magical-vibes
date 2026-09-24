package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LagomosHandOfHatred.class, GrizzlyBears.class})
class LagomosHandOfHatredTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a hasty trampling 2/1 Elemental at the beginning of your combat")
    void createsElementalAtBeginningOfCombat() {
        harness.addToBattlefield(player1, new LagomosHandOfHatred());

        advanceToCombat(player1);
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "Elemental");
        assertThat(elemental.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(elemental.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Sacrifices the combat token at the beginning of the next end step")
    void sacrificesElementalAtNextEndStep() {
        harness.addToBattlefield(player1, new LagomosHandOfHatred());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Elemental")).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.ensurePriority(player1);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    @DisplayName("Cannot search before five creatures have died")
    void cannotSearchBeforeFiveCreatureDeaths() {
        Permanent lagomos = addReadyLagomos(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(lagomos), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("five or more creatures died");
        assertThat(lagomos.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Searches the library for a card after five creatures have died")
    void searchesLibraryAfterFiveCreatureDeaths() {
        Permanent lagomos = addReadyLagomos(player1);
        gd.creatureDeathCountThisTurn.put(player1.getId(), 5);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lagomos), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(lagomos.isTapped()).isTrue();
    }

    private Permanent addReadyLagomos(Player player) {
        return addCreatureReady(player, new LagomosHandOfHatred());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
