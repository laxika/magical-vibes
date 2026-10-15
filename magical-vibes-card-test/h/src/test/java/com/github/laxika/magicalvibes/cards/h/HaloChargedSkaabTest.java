package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.t.TemporalCleansing;
import com.github.laxika.magicalvibes.cards.v.VolcanicSpite;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@com.github.laxika.magicalvibes.testutil.CardUsed({HaloChargedSkaab.class, GrizzlyBears.class, Shock.class,
        InvasionOfZendikar.class, TemporalCleansing.class, VolcanicSpite.class})
class HaloChargedSkaabTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, each player mills two cards and you may put an eligible card on top of your library")
    void millsEachPlayerAndReturnsEligibleCard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new Shock(), new GrizzlyBears()));
        harness.castFromHand(player1, new HaloChargedSkaab(), "{4}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.GraveyardChoice choice =
                (PendingInteraction.GraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).contains(0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Shock");
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Declining the may ability leaves the graveyard unchanged")
    void decliningMayAbilityLeavesGraveyardUnchanged() {
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(shock));
        harness.castFromHand(player1, new HaloChargedSkaab(), "{4}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
    }

    @Test
    @DisplayName("The may ability only offers instant, sorcery, and battle cards")
    void mayAbilityFiltersGraveyardCards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.castFromHand(player1, new HaloChargedSkaab(), "{4}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                (PendingInteraction.GraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1);
    }
    @Test
    @DisplayName("A just-milled instant can be chosen, but the opponent's milled instant cannot")
    void returnsJustMilledInstantFromOwnGraveyard() {
        Card ownInstant = new VolcanicSpite();
        Card opponentInstant = new VolcanicSpite();
        Card remaining = new HaloChargedSkaab();
        harness.setLibrary(player1, List.of(ownInstant, new HaloChargedSkaab(), remaining));
        harness.setLibrary(player2, List.of(opponentInstant, new HaloChargedSkaab()));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        harness.castFromHand(player1, new HaloChargedSkaab(), "{4}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                (PendingInteraction.GraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownInstant, remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ownInstant);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentInstant);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A sorcery can be returned when each library has fewer than two cards")
    void returnsSorceryAfterMillingShortLibraries() {
        Card sorcery = new TemporalCleansing();
        Card milled = new HaloChargedSkaab();
        harness.setLibrary(player1, List.of(milled));
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setGraveyard(player2, List.of());

        harness.castFromHand(player1, new HaloChargedSkaab(), "{4}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sorcery, milled);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A battle card is eligible in the graveyard and returns to the top of the library")
    void returnsBattleCard() {
        Card battle = new InvasionOfZendikar();
        harness.setLibrary(player1, List.of(new HaloChargedSkaab(), new HaloChargedSkaab()));
        harness.setLibrary(player2, List.of(new HaloChargedSkaab(), new HaloChargedSkaab()));
        harness.setGraveyard(player1, List.of(battle));

        harness.castFromHand(player1, new HaloChargedSkaab(), "{4}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                (PendingInteraction.GraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(battle);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(battle);
    }

    @Test
    @DisplayName("Accepting with no eligible card completes the ability without returning a creature")
    void noEligibleCardDoesNotPreventMillingOrCompletion() {
        Card creature = new HaloChargedSkaab();
        Card opponentInstant = new VolcanicSpite();
        harness.setLibrary(player1, List.of(creature));
        harness.setLibrary(player2, List.of(opponentInstant));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        harness.castFromHand(player1, new HaloChargedSkaab(), "{4}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentInstant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

}
