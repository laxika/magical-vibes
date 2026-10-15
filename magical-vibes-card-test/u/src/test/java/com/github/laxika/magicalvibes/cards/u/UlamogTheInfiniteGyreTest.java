package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.Deprive;
import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.g.GideonJura;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KeeningStone;
import com.github.laxika.magicalvibes.cards.r.Regress;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({UlamogTheInfiniteGyre.class, GrizzlyBears.class, Island.class, DiabolicEdict.class, Deprive.class, KeeningStone.class, GideonJura.class, Regress.class})
class UlamogTheInfiniteGyreTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Ulamog destroys the targeted permanent before Ulamog resolves")
    void castingDestroysTargetedPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UlamogTheInfiniteGyre()));
        harness.addMana(player1, ManaColor.COLORLESS, 11);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Ulamog, the Infinite Gyre");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ulamog, the Infinite Gyre");
    }

    @Test
    @DisplayName("Ulamog's annihilator makes the defending player sacrifice four permanents")
    void annihilatorFour() {
        Permanent ulamog = addCreatureReady(player1, new UlamogTheInfiniteGyre());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ulamog)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("When Ulamog goes to a graveyard, its owner's graveyard is shuffled into their library")
    void shufflesItsOwnersGraveyardIntoLibrary() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new UlamogTheInfiniteGyre());

        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, player1.getId());

        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).extracting(Card::getName)
                .containsExactlyInAnyOrder("Island", "Ulamog, the Infinite Gyre", "Grizzly Bears",
                        "Diabolic Edict");
    }

    @Test
    @DisplayName("Ulamog cannot target a player with its cast trigger")
    void cannotTargetPlayer() {
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new UlamogTheInfiniteGyre()));
        harness.addMana(player1, ManaColor.COLORLESS, 11);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, legalTarget.getId());
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player2, "Island");
    }

    @Test
    void castTriggerSurvivesCounteringSpellAndGraveyardTriggerUsesStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent costLand = harness.addToBattlefieldAndReturn(player2, new Island());
        UlamogTheInfiniteGyre ulamog = new UlamogTheInfiniteGyre();
        Island graveyardCard = new Island();
        Island libraryCard = new Island();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(ulamog));
        harness.addMana(player1, ManaColor.COLORLESS, 11);
        harness.setHand(player2, List.of(new Deprive()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passPriority(player1);
        harness.castInstantWithSacrifice(player2, 0, ulamog.getId(), costLand.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ulamog, graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Ulamog, the Infinite Gyre");

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(ulamog, graveyardCard, libraryCard);
        harness.assertOnBattlefield(player1, "Island");

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target.getCard());
        harness.assertInGraveyard(player2, "Deprive");
        harness.assertNotOnBattlefield(player1, "Ulamog, the Infinite Gyre");
    }

    @Test
    void annihilatorSacrificesAllPermanentsWhenDefenderHasFewerThanFour() {
        addCreatureReady(player1, new UlamogTheInfiniteGyre());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Ulamog, the Infinite Gyre");
    }

    @Test
    void defendingPlayerChoosesFourPermanentsToSacrifice() {
        addCreatureReady(player1, new UlamogTheInfiniteGyre());
        List<Permanent> lands = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new Island()))
                .toList();

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player2,
                lands.subList(1, 5).stream().map(Permanent::getId).toList());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(lands.getFirst());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrderElementsOf(lands.subList(1, 5).stream().map(Permanent::getCard).toList());
    }

    @Test
    void castTriggerCannotDestroyIndestructibleUlamog() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UlamogTheInfiniteGyre());
        harness.setHand(player1, List.of(new UlamogTheInfiniteGyre()));
        harness.addMana(player1, ManaColor.COLORLESS, 11);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ulamog, the Infinite Gyre");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void enteringBattlefieldWithoutCastingDoesNotDestroyPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.enterBattlefieldAndReturn(player1, new UlamogTheInfiniteGyre());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertOnBattlefield(player1, "Ulamog, the Infinite Gyre");
    }

    @Test
    void sacrificedUlamogShufflesOwnersGraveyardRatherThanControllers() {
        UlamogTheInfiniteGyre ulamog = new UlamogTheInfiniteGyre();
        ulamog.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, ulamog);
        Island ownersCard = new Island();
        Island controllersCard = new Island();
        harness.setGraveyard(player1, List.of(ownersCard));
        harness.setGraveyard(player2, List.of(controllersCard));
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ulamog, ownersCard);
        harness.assertNotOnBattlefield(player2, "Ulamog, the Infinite Gyre");
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(ulamog, ownersCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(controllersCard);
    }

    @Test
    void millingUlamogShufflesTheEntireGraveyardAfterMillFinishes() {
        harness.addToBattlefield(player1, new KeeningStone());
        Island first = new Island();
        Island second = new Island();
        Island milledLand = new Island();
        Island remainingLand = new Island();
        UlamogTheInfiniteGyre ulamog = new UlamogTheInfiniteGyre();
        harness.setGraveyard(player2, List.of(first, second));
        harness.setLibrary(player2, List.of(ulamog, milledLand, remainingLand));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(first, second, ulamog, milledLand);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingLand);

        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactlyInAnyOrder(first, second, ulamog, milledLand, remainingLand);
        harness.assertNotOnBattlefield(player2, "Ulamog, the Infinite Gyre");
    }

    @Test
    void canCastWithoutAnyPermanentToTarget() {
        harness.setHand(player1, List.of(new UlamogTheInfiniteGyre()));
        harness.addMana(player1, ManaColor.COLORLESS, 11);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ulamog, the Infinite Gyre");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void annihilatorStillAppliesWhenAttackedPlaneswalkerLeavesInResponse() {
        addCreatureReady(player1, new UlamogTheInfiniteGyre());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GideonJura());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Island());
        }
        harness.setHand(player2, List.of(new Regress()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
        harness.passPriority(player1);

        harness.castInstant(player2, 0, planeswalker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Gideon Jura");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(4);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Regress", "Island", "Island", "Island", "Island");
    }
}
