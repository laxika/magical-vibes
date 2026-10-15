package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SayItsName;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HedgeShredder.class, Forest.class, GrizzlyBears.class, SayItsName.class})
class HedgeShredderTest extends BaseCardTest {

    @Test
    @DisplayName("Crew 1 animates Hedge Shredder and taps the chosen creature")
    void crewAnimatesVehicle() {
        Permanent shredder = addReadyShredder();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shredder.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking may mill two cards")
    void attackingMayMillTwoCards() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        crewShredder();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("Declining the attack trigger does not mill")
    void decliningAttackTriggerDoesNotMill() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        crewShredder();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Lands milled in one event return to the battlefield tapped")
    void milledLandsReturnTapped() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        crewShredder();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Forest")).hasSize(2).allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only land cards return from a mixed mill event")
    void onlyMilledLandsReturn() {
        Card forest = new Forest();
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, nonland));
        crewShredder();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Forest")).hasSize(1).allMatch(Permanent::isTapped);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonland);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("The optional mill cannot be chosen with fewer than two cards")
    void cannotChooseMillWithShortLibrary(int librarySize) {
        List<Card> library = librarySize == 0 ? List.of() : List.of(new Forest());
        harness.setLibrary(player1, library);
        crewShredder();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An uncrewed Vehicle returns lands milled by another spell, but not old graveyard lands")
    void externalMillReturnsOnlyNewLands() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new HedgeShredder());
        Card oldLand = new Forest();
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        Card thirdLand = new Forest();
        harness.setGraveyard(player1, List.of(oldLand));
        harness.setLibrary(player1, List.of(firstLand, secondLand, thirdLand));

        harness.castFromHand(player1, new SayItsName(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Forest")).hasSize(3).allMatch(Permanent::isTapped);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldLand)
                .doesNotContain(firstLand, secondLand, thirdLand);
    }

    @Test
    @DisplayName("An opponent's milled lands do not return")
    void opponentsMilledLandsStayInGraveyard() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new HedgeShredder());
        Card land = new Forest();
        harness.setLibrary(player2, List.of(land));

        harness.castFromHand(player2, new SayItsName(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(findPermanents(player2, "Forest")).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(land);
    }

    private Permanent addReadyShredder() {
        return addCreatureReady(player1, new HedgeShredder());
    }

    private Permanent crewShredder() {
        Permanent shredder = addReadyShredder();
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        return shredder;
    }
}
