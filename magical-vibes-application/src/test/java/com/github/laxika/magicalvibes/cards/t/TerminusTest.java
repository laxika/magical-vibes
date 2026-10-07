package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Terminus.class, GrizzlyBears.class, LlanowarElves.class, Plains.class})
class TerminusTest extends BaseCardTest {

    @Test
    @DisplayName("Puts all creatures on the bottom of their owners' libraries")
    void bottomsAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new Terminus()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Indestructible does not save a creature from Terminus")
    void indestructibleDoesNotSave() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.setHand(player1, List.of(new Terminus()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Miracle cast for {W} off the first draw wipes the board")
    void miracleCastWipesBoard() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.addToBattlefield(player2, new LlanowarElves());

            Terminus terminus = new Terminus();
            harness.setLibrary(player1, List.of(terminus));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
            harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
            harness.handleMayAbilityChosen(player1, true); // reveal

            harness.passBothPriorities(); // resolve miracle trigger → cast prompt
            harness.handleMayAbilityChosen(player1, true); // cast for miracle cost
            harness.passBothPriorities(); // resolve Terminus

            harness.assertNotOnBattlefield(player2, "Llanowar Elves");
            assertThat(gd.playerDecks.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Llanowar Elves"));
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        });
    }

    @Test
    @DisplayName("Declining the miracle reveal leaves the card in hand")
    void decliningRevealLeavesInHand() {
        Terminus terminus = new Terminus();
        harness.setLibrary(player1, List.of(terminus));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(terminus.getId()));
    }

    @Test
    @DisplayName("Does nothing when no creatures are on the battlefield")
    void doesNothingWhenNoCreatures() {
        harness.setHand(player1, List.of(new Terminus()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Terminus");
    }

    @Test
    @DisplayName("The owner chooses the order of multiple creatures on the bottom")
    void ownerChoosesBottomOrder() {
        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        Terminus existingTop = new Terminus();
        harness.setLibrary(player1, List.of(existingTop));
        harness.addToBattlefield(player1, bears);
        harness.addToBattlefield(player1, elves);
        harness.setHand(player1, List.of(new Terminus()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(existingTop, elves, bears);
    }

    @Test
    @DisplayName("A creature controlled by an opponent goes to its owner's library bottom")
    void bottomsStolenCreatureInOwnersLibrary() {
        GrizzlyBears bears = new GrizzlyBears();
        bears.setOwnerId(player1.getId());
        Terminus existingTop = new Terminus();
        harness.setLibrary(player1, List.of(existingTop));
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player2, bears);
        harness.setHand(player1, List.of(new Terminus()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(existingTop, bears);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The second card drawn in a turn cannot be revealed for miracle")
    void secondDrawDoesNotOfferMiracle() {
        GrizzlyBears first = new GrizzlyBears();
        Terminus terminus = new Terminus();
        harness.setLibrary(player1, List.of(first, terminus));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(first, terminus);
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the miracle cast leaves the board and mana unchanged")
    void decliningCastLeavesBoardUnchanged() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            Terminus terminus = new Terminus();
            harness.setLibrary(player1, List.of(terminus));
            harness.addToBattlefield(player2, new LlanowarElves());
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
            harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.playerHands.get(player1.getId())).contains(terminus);
            harness.assertOnBattlefield(player2, "Llanowar Elves");
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    @DisplayName("Miracle cannot cast the revealed card after it leaves the hand")
    void miracleDoesNotCastCardThatLeftHand() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            Terminus terminus = new Terminus();
            harness.setLibrary(player1, List.of(terminus));
            harness.addToBattlefield(player2, new LlanowarElves());
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
            harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
            harness.handleMayAbilityChosen(player1, true);
            harness.setHand(player1, List.of());
            harness.setGraveyard(player1, List.of(terminus));

            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Llanowar Elves");
            harness.assertInGraveyard(player1, "Terminus");
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingMayAbilities).isEmpty();
        });
    }

    @Test
    @DisplayName("Terminus leaves noncreature permanents on the battlefield")
    void leavesNoncreaturePermanents() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Plains());
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);
        Terminus existingTop = new Terminus();
        harness.setLibrary(player2, List.of(existingTop));
        harness.setHand(player1, List.of(new Terminus()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player2, "Plains");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTop, bears);
    }

    @Test
    @DisplayName("Miracle can cast Terminus on an opponent's turn")
    void miracleCastsDuringOpponentsTurn() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            gd.activePlayerId = player2.getId();
            gd.currentStep = TurnStep.PRECOMBAT_MAIN;
            Terminus terminus = new Terminus();
            harness.setLibrary(player1, List.of(terminus));
            harness.addToBattlefield(player2, new LlanowarElves());
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
            harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Llanowar Elves");
            harness.assertInGraveyard(player1, "Terminus");
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        });
    }
}
