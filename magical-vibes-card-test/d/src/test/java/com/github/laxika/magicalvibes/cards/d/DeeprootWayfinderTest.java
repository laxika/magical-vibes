package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeeprootWayfinder.class, Forest.class, InvasionOfZendikar.class})
class DeeprootWayfinderTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage surveils one, then returns a land card tapped")
    void combatDamageSurveilsThenReturnsLandTapped() {
        Card topCard = new DeeprootWayfinder();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setGraveyard(player1, List.of(land, new DeeprootWayfinder()));
        addWayfinderAttacking();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 0);

        Permanent returnedLand = findPermanent(player1, "Forest");
        assertThat(returnedLand.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Declining the land return leaves the graveyard unchanged")
    void declinesLandReturn() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setLibrary(player1, List.of(new DeeprootWayfinder()));
        addWayfinderAttacking();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(land.getId()));
    }

    @Test
    void landReturnContinuesDuringOriginalAbilityResolution() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setGraveyard(player1, List.of());
        addWayfinderAttacking();
        resolveCombat();
        harness.passBothPriorities();

        harness.withAutoStop(gd.currentStep,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void canReturnLandJustPutIntoGraveyardBySurveil() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setGraveyard(player1, List.of());
        addWayfinderAttacking();
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryStillAllowsReturningAnExistingLand() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(land));
        addWayfinderAttacking();
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void keepingTopCardStillAllowsReturningAnExistingLand() {
        Card topCard = new DeeprootWayfinder();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setGraveyard(player1, List.of(land));
        addWayfinderAttacking();
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void combatDamageToBattleAlsoSurveilsAndReturnsLand() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setGraveyard(player1, List.of());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        Permanent wayfinder = addCreatureReady(player1, new DeeprootWayfinder());
        wayfinder.setAttacking(true);
        wayfinder.setAttackTarget(battle.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.resolveCombatDamage();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertLife(player2, 20);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void nonlandCardsAndOpponentsLandsCannotBeReturned() {
        Card nonland = new DeeprootWayfinder();
        Card opponentsLand = new Forest();
        harness.setLibrary(player1, List.of(nonland));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentsLand));
        addWayfinderAttacking();
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsLand);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    void combatDamageToCreatureDoesNotTriggerAbility() {
        Card topCard = new Forest();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setGraveyard(player1, List.of(land));
        addCreatureReady(player1, new DeeprootWayfinder());
        addCreatureReady(player2, new DeeprootWayfinder());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    private void addWayfinderAttacking() {
        addCreatureReady(player1, new DeeprootWayfinder());
        declareAttackers(List.of(0));
    }
}
