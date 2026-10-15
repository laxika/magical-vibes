package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KellanTheKid.class, Firebolt.class, DoomedTraveler.class, GrizzlyBears.class, Mountain.class})
class KellanTheKidTest extends BaseCardTest {

    @Test
    void castsAnEligiblePermanentFromHandAfterCastingFromGraveyard() {
        harness.addToBattlefield(player1, new KellanTheKid());
        harness.setGraveyard(player1, List.of(new Firebolt()));
        harness.setHand(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Doomed Traveler")).isNotNull();
    }

    @Test
    void offersALandWhenThePermanentSpellIsTooExpensive() {
        harness.addToBattlefield(player1, new KellanTheKid());
        harness.setGraveyard(player1, List.of(new Firebolt()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 1);

        assertThat(findPermanent(player1, "Mountain")).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    void doesNotTriggerForASpellCastFromHand() {
        harness.addToBattlefield(player1, new KellanTheKid());
        harness.setHand(player1, List.of(new Firebolt(), new DoomedTraveler()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void decliningOneCandidateThenCastingAnotherDoesNotAlsoAllowALand() {
        harness.addToBattlefield(player1, new KellanTheKid());
        harness.setGraveyard(player1, List.of(new Firebolt()));
        harness.setHand(player1, List.of(new DoomedTraveler(), new DoomedTraveler(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFlashback(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Doomed Traveler");
        harness.assertInHand(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Mountain");
    }

    @Test
    void decliningAllCandidatesAllowsOnlyOneLand() {
        harness.addToBattlefield(player1, new KellanTheKid());
        harness.setGraveyard(player1, List.of(new Firebolt()));
        harness.setHand(player1, List.of(new DoomedTraveler(), new DoomedTraveler(),
                new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFlashback(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, 2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Mountain"))
                .hasSize(1);
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    void canDeclineBothAnEligiblePermanentAndTheLand() {
        harness.addToBattlefield(player1, new KellanTheKid());
        harness.setGraveyard(player1, List.of(new Firebolt()));
        harness.setHand(player1, List.of(new DoomedTraveler(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFlashback(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Doomed Traveler");
        harness.assertInHand(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Doomed Traveler");
        harness.assertNotOnBattlefield(player1, "Mountain");
    }

    @Test
    void doesNotTriggerForAnOpponentsSpellCastFromGraveyard() {
        harness.addToBattlefield(player1, new KellanTheKid());
        harness.setHand(player1, List.of(new DoomedTraveler(), new Mountain()));
        harness.setGraveyard(player2, List.of(new Firebolt()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castAndResolveFlashback(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertInHand(player1, "Doomed Traveler");
        harness.assertInHand(player1, "Mountain");
        harness.assertLife(player1, 18);
    }
}
