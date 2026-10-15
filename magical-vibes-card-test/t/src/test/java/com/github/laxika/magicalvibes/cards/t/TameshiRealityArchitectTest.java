package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoShintaiOfSharedPurpose;
import com.github.laxika.magicalvibes.cards.m.MoonsnarePrototype;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RiversRebuke;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TameshiRealityArchitect.class, Boomerang.class, FountainOfYouth.class,
        GrizzlyBears.class, Plains.class, GoShintaiOfSharedPurpose.class,
        MoonsnarePrototype.class, RiversRebuke.class})
class TameshiRealityArchitectTest extends BaseCardTest {

    @Test
    void returnsAnArtifactFromTheGraveyardUsingX() {
        FountainOfYouth fountain = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(fountain));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefieldAndReturn(player1, new TameshiRealityArchitect());
        harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 1, fountain.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(fountain);
    }

    @Test
    void cannotTargetACreatureCard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addToBattlefieldAndReturn(player1, new TameshiRealityArchitect());
        harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, 2, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void drawsOnlyOnceForMultipleNoncreaturePermanentsReturnedInOneTurn() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Boomerang(), new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addToBattlefieldAndReturn(player1, new TameshiRealityArchitect());
        Permanent firstFountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent secondFountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        harness.castAndResolveInstant(player1, 0, firstFountain.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, secondFountain.getId());

        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears")))
                .hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    void doesNotDrawWhenAcreatureIsReturnedToHand() {
        harness.setLibrary(player1, List.of(new FountainOfYouth()));
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addToBattlefieldAndReturn(player1, new TameshiRealityArchitect());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Fountain of Youth");
    }

    @Test
    void drawsWhenReturnedAlongsideANoncreaturePermanent() {
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addToBattlefield(player1, new TameshiRealityArchitect());
        harness.addToBattlefield(player1, new MoonsnarePrototype());
        harness.setHand(player2, List.of(new RiversRebuke()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Tameshi, Reality Architect");
        harness.assertInHand(player1, "Moonsnare Prototype");
        harness.assertInHand(player1, "Plains");
    }

    @Test
    void returnsAnEnchantmentCreatureWithManaValueEqualToX() {
        GoShintaiOfSharedPurpose shrine = new GoShintaiOfSharedPurpose();
        harness.setGraveyard(player1, List.of(shrine));
        harness.setLibrary(player1, List.of(new MoonsnarePrototype()));
        harness.addToBattlefield(player1, new TameshiRealityArchitect());
        harness.addToBattlefield(player1, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, 0, 4, shrine.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Go-Shintai of Shared Purpose");
        harness.assertNotInGraveyard(player1, "Go-Shintai of Shared Purpose");
        harness.assertInHand(player1, "Plains");
        harness.assertInHand(player1, "Moonsnare Prototype");
    }

    @Test
    void returnsAZeroManaArtifactWithXZero() {
        FountainOfYouth fountain = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(fountain));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addToBattlefield(player1, new TameshiRealityArchitect());
        harness.addToBattlefield(player1, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, 0, fountain.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void cannotTargetAnArtifactAboveX() {
        MoonsnarePrototype prototype = new MoonsnarePrototype();
        harness.setGraveyard(player1, List.of(prototype));
        harness.addToBattlefield(player1, new TameshiRealityArchitect());
        harness.addToBattlefield(player1, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, 0, prototype.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertInGraveyard(player1, "Moonsnare Prototype");
    }

    @Test
    void cannotTargetAnOpponentsGraveyard() {
        MoonsnarePrototype prototype = new MoonsnarePrototype();
        harness.setGraveyard(player2, List.of(prototype));
        harness.addToBattlefield(player1, new TameshiRealityArchitect());
        harness.addToBattlefield(player1, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, 1, prototype.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertInGraveyard(player2, "Moonsnare Prototype");
    }

    @Test
    void cannotActivateOutsideAMainPhase() {
        MoonsnarePrototype prototype = new MoonsnarePrototype();
        harness.setGraveyard(player1, List.of(prototype));
        harness.addToBattlefield(player1, new TameshiRealityArchitect());
        harness.addToBattlefield(player1, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, 1, prototype.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    void drawsForAnOpponentsNoncreaturePermanentReturningToHand() {
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addToBattlefield(player1, new TameshiRealityArchitect());
        Permanent prototype = harness.addToBattlefieldAndReturn(player2, new MoonsnarePrototype());

        harness.castAndResolveInstant(player1, 0, prototype.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Moonsnare Prototype");
        harness.assertInHand(player1, "Plains");
        harness.assertNotInHand(player2, "Plains");
    }

    @Test
    void cannotActivateWithoutALandToReturn() {
        MoonsnarePrototype prototype = new MoonsnarePrototype();
        harness.setGraveyard(player1, List.of(prototype));
        harness.addToBattlefield(player1, new TameshiRealityArchitect());
        harness.addToBattlefield(player2, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, 1, prototype.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Plains");
        harness.assertInGraveyard(player1, "Moonsnare Prototype");
    }

    @Test
    void canDrawAgainDuringTheOpponentsTurn() {
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains()));
        harness.setHand(player1, List.of(new Boomerang(), new Boomerang()));
        harness.addToBattlefield(player1, new TameshiRealityArchitect());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MoonsnarePrototype());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MoonsnarePrototype());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Plains"))).hasSize(1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Plains"))).hasSize(2);
    }
}
