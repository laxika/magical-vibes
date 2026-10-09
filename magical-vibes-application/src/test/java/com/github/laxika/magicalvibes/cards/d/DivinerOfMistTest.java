package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DivinerOfMist.class, Shock.class, LavaAxe.class, GrizzlyBears.class, Concentrate.class})
class DivinerOfMistTest extends BaseCardTest {

    @Test
    void attackMillsFourAndOffersOnlyEligibleGraveyardSpell() {
        Shock shock = new Shock();
        Shock secondShock = new Shock();
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(shock, secondShock, new LavaAxe(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock()));
        Permanent diviner = addCreatureReady(player1, new DivinerOfMist());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(diviner)));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        var choice = gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).containsExactlyInAnyOrder(shock, secondShock);
    }

    @Test
    void cannotSaveTheFreeCastForAfterTheAttackTriggerResolves() {
        Shock shock = new Shock();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(shock));
        Permanent diviner = addCreatureReady(player1, new DivinerOfMist());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(diviner)));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void castsSorceryDuringAttackTriggerWithoutManaAndExilesItAfterResolution() {
        Concentrate concentrate = new Concentrate();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(concentrate));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent diviner = addCreatureReady(player1, new DivinerOfMist());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(diviner)));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertNotInGraveyard(player1, "Concentrate");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(concentrate.getId()));
    }

    @Test
    void newlyMilledSpellIsAvailableEvenWhenLibraryHasFewerThanFourCards() {
        Concentrate concentrate = new Concentrate();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setLibrary(player1, List.of(concentrate, new GrizzlyBears()));
        Permanent diviner = addCreatureReady(player1, new DivinerOfMist());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(diviner)));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        var choice = gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).containsExactlyInAnyOrder(shock, concentrate);
    }

    @Test
    void decliningTheCastStillMillsFourCards() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent diviner = addCreatureReady(player1, new DivinerOfMist());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(diviner)));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5).contains(shock);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
