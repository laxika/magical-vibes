package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FblthpTheLost;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProteusStaff.class, FblthpTheLost.class, Forest.class, GrizzlyBears.class,
        LlanowarElves.class, Shock.class})
class ProteusStaffTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the target on its owner's library bottom and polymorphs its controller's library")
    void putsTargetOnBottomAndRevealsUntilCreature() {
        harness.addToBattlefield(player1, new ProteusStaff());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card shock1 = new Shock();
        Card shock2 = new Shock();
        Card creature = new GrizzlyBears();
        Card tail = new Forest();
        harness.setLibrary(player1, List.of(shock1, shock2, creature, tail));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Proteus Staff", "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(reorder.indexOf(shock2), reorder.indexOf(shock1))));

        assertThat(deck).containsExactly(tail, target.getCard(), shock2, shock1);
    }

    @Test
    @DisplayName("Uses the target creature's controller's library")
    void usesTargetControllersLibrary() {
        harness.addToBattlefield(player1, new ProteusStaff());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card player1Top = new Shock();
        Card player2Noncreature = new Shock();
        Card player2Creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(player1Top));
        harness.setLibrary(player2, List.of(player2Noncreature, player2Creature));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Top);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), player2Noncreature);
    }

    @Test
    @DisplayName("Uses the target's owner library for the target and its controller's library for the reveal")
    void separatesTargetOwnerAndControllerLibraries() {
        harness.addToBattlefield(player1, new ProteusStaff());
        LlanowarElves targetCard = new LlanowarElves();
        targetCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card ownerTop = new Forest();
        Card controllerNoncreature = new Shock();
        harness.setLibrary(player1, List.of(ownerTop));
        harness.setLibrary(player2, List.of(controllerNoncreature));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownerTop, targetCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(controllerNoncreature);
    }

    @Test
    @DisplayName("Preserves library entry for the creature put onto the battlefield")
    void preservesLibraryEntryForFoundCreature() {
        harness.addToBattlefield(player1, new ProteusStaff());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        FblthpTheLost foundCreature = new FblthpTheLost();
        Card drawn1 = new Shock();
        Card drawn2 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(foundCreature, drawn1, drawn2));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn1, drawn2);
    }

    @Test
    @DisplayName("Finds the targeted creature itself when it is the last creature in the library")
    void returnsTargetWhenNoOtherCreatureExists() {
        Card staff = new ProteusStaff();
        harness.addToBattlefield(player1, staff);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card first = new Forest();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(staff, target.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).doesNotContain(target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
    }

    @Test
    @DisplayName("An empty controller library still allows the target to go to its owner's library")
    void emptyControllerLibraryDoesNotPreventMovingTarget() {
        harness.addToBattlefield(player1, new ProteusStaff());
        Card targetCard = new LlanowarElves();
        targetCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(targetCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Does not reveal any cards when the target dies before resolution")
    void doesNotRevealWhenTargetBecomesIllegal() {
        harness.addToBattlefield(player1, new ProteusStaff());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getName()).containsExactly("Proteus Staff");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The target's controller orders their entire library when no creature is found")
    void controllerReordersEntireLibraryWithoutCreatures() {
        harness.addToBattlefield(player1, new ProteusStaff());
        Card targetCard = new LlanowarElves();
        targetCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card first = new Shock();
        Card second = new Forest();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(first, second));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(targetCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second, first);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void requiresSorcerySpeed() {
        harness.addToBattlefield(player1, new ProteusStaff());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        harness.addToBattlefield(player1, new ProteusStaff());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
