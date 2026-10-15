package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MachineOverMatter;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerisianMindbreaker.class, Island.class, MachineOverMatter.class, TeferiTemporalPilgrim.class})
class TerisianMindbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking mills half the defending player's library rounded up")
    void attackingMillsHalfDefendingLibraryRoundedUp() {
        addCreatureReady(player1, new TerisianMindbreaker());
        harness.setLibrary(player2, List.of(
                new Island(), new Island(), new Island(), new Island(), new Island()));
        int ownLibrarySize = gd.playerDecks.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(ownLibrarySize);
    }

    @Test
    @DisplayName("Unearth returns Terisian Mindbreaker with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new TerisianMindbreaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent mindbreaker = findPermanent(player1, "Terisian Mindbreaker");
        assertThat(mindbreaker.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Terisian Mindbreaker");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Terisian Mindbreaker"));
    }

    @Test
    void attackingMillsExactlyHalfAnEvenLibrary() {
        addCreatureReady(player1, new TerisianMindbreaker());
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island(), new Island()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void millingAnEmptyLibraryDoesNotCauseALoss() {
        addCreatureReady(player1, new TerisianMindbreaker());
        harness.setLibrary(player2, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void millAmountUsesLibrarySizeAtResolution() {
        addCreatureReady(player1, new TerisianMindbreaker());
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island(), new Island(), new Island()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));
            resolveAllTriggers();
        });

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void attackTriggerStillMillsAfterAttackedPlaneswalkerLeaves() {
        addCreatureReady(player1, new TerisianMindbreaker());
        Permanent teferi = harness.addToBattlefieldAndReturn(player2, new TeferiTemporalPilgrim());
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player1, List.of(0), Map.of(0, teferi.getId()));
            harness.castInstant(player1, 0, teferi.getId());
            harness.passBothPriorities();
            harness.assertNotOnBattlefield(player2, "Teferi, Temporal Pilgrim");
            resolveAllTriggers();
        });

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void unearthedMindbreakerCanAttackImmediately() {
        harness.setGraveyard(player1, List.of(new TerisianMindbreaker()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void bouncingUnearthedMindbreakerExilesItInstead() {
        harness.setGraveyard(player1, List.of(new TerisianMindbreaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent mindbreaker = findPermanent(player1, "Terisian Mindbreaker");
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, mindbreaker.getId());

        harness.assertNotOnBattlefield(player1, "Terisian Mindbreaker");
        harness.assertNotInHand(player1, "Terisian Mindbreaker");
        harness.assertNotInGraveyard(player1, "Terisian Mindbreaker");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(mindbreaker.getCard());
    }

    @Test
    void unearthCannotBeActivatedDuringCombat() {
        harness.setGraveyard(player1, List.of(new TerisianMindbreaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Terisian Mindbreaker");
        harness.assertNotOnBattlefield(player1, "Terisian Mindbreaker");
    }

    @Test
    void unearthRequiresThreeBlueMana() {
        harness.setGraveyard(player1, List.of(new TerisianMindbreaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Terisian Mindbreaker");
        harness.assertNotOnBattlefield(player1, "Terisian Mindbreaker");
    }
}
