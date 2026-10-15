package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IceMagic.class, GrizzlyBears.class, Island.class})
class IceMagicTest extends BaseCardTest {

    @Test
    @DisplayName("Blizzard returns the target creature to its owner's hand")
    void blizzardReturnsCreatureToHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(0, target.getId(), 2);

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Blizzara lets the target creature's owner keep it on top")
    void blizzaraPutsCreatureOnTopOrBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card libraryCard = new Island();
        harness.setLibrary(player2, List.of(libraryCard));

        cast(1, target.getId(), 4);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), libraryCard);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Blizzaga shuffles the target creature into its owner's library")
    void blizzagaShufflesCreatureIntoLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card libraryCard = new Island();
        harness.setLibrary(player2, List.of(libraryCard));

        cast(2, target.getId(), 8);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(libraryCard, target.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ice Magic cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new IceMagic()));
        addMana(2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void blizzaraOwnerCanChooseBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player2, List.of(first, second));

        cast(1, target.getId(), 4);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Bottom"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first, second, target.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void blizzaraChoiceAndLibraryBelongToOwnerRatherThanController() {
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        Card ownerLibraryCard = new Island();
        Card controllerLibraryCard = new Island();
        harness.setLibrary(player2, List.of(ownerLibraryCard));
        harness.setLibrary(player1, List.of(controllerLibraryCard));

        cast(1, target.getId(), 4);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(ownerLibraryCard, creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerLibraryCard);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void blizzardReturnsStolenCreatureToOwner() {
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);

        cast(0, target.getId(), 2);

        assertThat(gd.playerHands.get(player2.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void blizzagaShufflesStolenCreatureIntoOwnersLibrary() {
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        Card ownerLibraryCard = new Island();
        Card controllerLibraryCard = new Island();
        harness.setLibrary(player2, List.of(ownerLibraryCard));
        harness.setLibrary(player1, List.of(controllerLibraryCard));

        cast(2, target.getId(), 8);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(ownerLibraryCard, creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerLibraryCard);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void blizzaraRequiresAdditionalMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IceMagic()));
        addMana(3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Ice Magic");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void blizzagaRequiresSecondBlueMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IceMagic()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Ice Magic");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void blizzaraDoesNothingWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card libraryCard = new Island();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new IceMagic()));
        addMana(4);
        harness.castInstant(player1, 0, 1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void cast(int mode, java.util.UUID targetId, int totalMana) {
        harness.setHand(player1, List.of(new IceMagic()));
        addMana(totalMana);
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }

    private void addMana(int totalMana) {
        int blueMana = totalMana == 8 ? 2 : 1;
        harness.addMana(player1, ManaColor.BLUE, blueMana);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - blueMana);
    }

}
