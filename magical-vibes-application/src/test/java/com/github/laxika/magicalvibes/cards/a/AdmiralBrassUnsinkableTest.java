package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SirenStormtamer;
import com.github.laxika.magicalvibes.cards.t.TalasScout;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdmiralBrassUnsinkable.class, TalasScout.class, GrizzlyBears.class, SirenStormtamer.class})
class AdmiralBrassUnsinkableTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, its controller mills four cards")
    void millsFourCardsOnEntry() {
        List<Card> library = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, library);

        harness.enterBattlefieldAndReturn(player1, new AdmiralBrassUnsinkable());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    @Test
    @DisplayName("At the beginning of combat, it returns a Pirate as a 4/4 with haste and a finality counter")
    void returnsPirateAsHastyFourFourWithFinality() {
        Card pirate = new TalasScout();
        harness.setGraveyard(player1, List.of(pirate));
        harness.addToBattlefield(player1, new AdmiralBrassUnsinkable());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of(pirate.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Talas Scout");
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(4);
    }

    @Test
    @DisplayName("It cannot target a non-Pirate creature card")
    void cannotTargetNonPirateCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new AdmiralBrassUnsinkable());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void mayDeclineReturnAtResolutionAfterChoosingTarget() {
        Card pirate = new SirenStormtamer();
        harness.setGraveyard(player1, List.of(pirate));
        harness.addToBattlefield(player1, new AdmiralBrassUnsinkable());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handleMultipleCardsChosen(player1, List.of(pirate.getId()));
        harness.assertInGraveyard(player1, "Siren Stormtamer");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Siren Stormtamer");
        harness.assertNotOnBattlefield(player1, "Siren Stormtamer");
    }

    @Test
    void millsRemainingCardsWhenLibraryHasFewerThanFour() {
        List<Card> library = List.of(new SirenStormtamer(), new SirenStormtamer());
        harness.setLibrary(player1, library);

        harness.enterBattlefieldAndReturn(player1, new AdmiralBrassUnsinkable());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void doesNotReturnPiratesDuringOpponentsCombat() {
        Card pirate = new SirenStormtamer();
        harness.setGraveyard(player1, List.of(pirate));
        harness.addToBattlefield(player1, new AdmiralBrassUnsinkable());

        harness.forceActivePlayer(player2);
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Siren Stormtamer");
        harness.assertNotOnBattlefield(player1, "Siren Stormtamer");
    }

    @Test
    void hasteExpiresButBasePowerToughnessAndFinalityRemain() {
        Card pirate = new SirenStormtamer();
        harness.setGraveyard(player1, List.of(pirate));
        harness.addToBattlefield(player1, new AdmiralBrassUnsinkable());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handleMultipleCardsChosen(player1, List.of(pirate.getId()));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        }
        Permanent returned = findPermanent(player1, "Siren Stormtamer");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(4);
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    void cannotReturnPirateFromOpponentsGraveyard() {
        Card pirate = new SirenStormtamer();
        harness.setGraveyard(player2, List.of(pirate));
        harness.addToBattlefield(player1, new AdmiralBrassUnsinkable());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Siren Stormtamer");
        harness.assertNotOnBattlefield(player1, "Siren Stormtamer");
        harness.assertNotOnBattlefield(player2, "Siren Stormtamer");
    }

    @Test
    void finalityExilesReturnedPirateWhenSacrificed() {
        Card pirate = new SirenStormtamer();
        harness.setGraveyard(player1, List.of(pirate));
        harness.addToBattlefield(player1, new AdmiralBrassUnsinkable());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handleMultipleCardsChosen(player1, List.of(pirate.getId()));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        }

        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(
                gd, findPermanent(player1, "Siren Stormtamer"));

        harness.assertNotOnBattlefield(player1, "Siren Stormtamer");
        harness.assertNotInGraveyard(player1, "Siren Stormtamer");
        assertThat(gd.findExiledCard(pirate.getId())).isNotNull();
    }
}
