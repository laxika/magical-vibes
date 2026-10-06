package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.b.BindTheMonster;
import com.github.laxika.magicalvibes.cards.c.CravenHulk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RunAshore.class, GrizzlyBears.class, FountainOfYouth.class, Island.class,
        CravenHulk.class, BindTheMonster.class})
class RunAshoreTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode puts the target on top of its owner's library")
    void firstModePutsTargetOnTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card existingTop = new Island();
        harness.setLibrary(player2, List.of(existingTop));

        cast(new int[]{0}, List.of(target.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), existingTop);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Run Ashore");
    }

    @Test
    @DisplayName("The second mode returns the target to its owner's hand")
    void secondModeReturnsTargetToHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{1}, List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Run Ashore");
    }

    @Test
    @DisplayName("Both modes resolve in order")
    void bothModesResolveInOrder() {
        Permanent libraryTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent handTarget = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Card existingTop = new Island();
        harness.setLibrary(player2, List.of(existingTop));

        cast(new int[]{0, 1}, List.of(libraryTarget.getId(), handTarget.getId()));

        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryTarget.getCard(), existingTop);
        harness.assertInHand(player2, "Fountain of Youth");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Run Ashore");
    }

    @Test
    @DisplayName("Neither mode can target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new RunAshore()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @CardUsed({RunAshore.class, CravenHulk.class, Island.class})
    void ownerCanChooseBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        Card existingTop = new Island();
        harness.setLibrary(player2, List.of(existingTop));

        cast(new int[]{0}, List.of(target.getId()));
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTop, target.getCard());
        harness.assertNotOnBattlefield(player2, "Craven Hulk");
        harness.assertInGraveyard(player1, "Run Ashore");
    }

    @Test
    @CardUsed({RunAshore.class, CravenHulk.class, Island.class})
    void bothModesCanTargetSamePermanentWithoutReturningItFromLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        Card existingTop = new Island();
        harness.setLibrary(player2, List.of(existingTop));

        cast(new int[]{0, 1}, List.of(target.getId(), target.getId()));
        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), existingTop);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(target.getCard());
        harness.assertNotOnBattlefield(player2, "Craven Hulk");
        harness.assertInGraveyard(player1, "Run Ashore");
    }

    @Test
    @CardUsed({RunAshore.class, CravenHulk.class, Island.class})
    void stolenPermanentsOwnerChoosesLibraryDestination() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CravenHulk());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        Card existingTop = new Island();
        harness.setLibrary(player2, List.of(existingTop));
        harness.setLibrary(player1, List.of());

        cast(new int[]{0}, List.of(target.getId()));

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Top"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTop, target.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Craven Hulk");
    }

    @Test
    @CardUsed({RunAshore.class, CravenHulk.class})
    void secondModeReturnsStolenPermanentToOwner() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CravenHulk());
        gd.stolenCreatures.put(target.getId(), player2.getId());

        cast(new int[]{1}, List.of(target.getId()));

        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target.getCard());
        harness.assertNotOnBattlefield(player1, "Craven Hulk");
    }

    @Test
    @CardUsed({RunAshore.class, CravenHulk.class, BindTheMonster.class, Island.class})
    void secondModeReturnsAuraBeforeUnattachedAuraIsPutIntoGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BindTheMonster());
        aura.setAttachedTo(creature.getId());
        Card existingTop = new Island();
        harness.setLibrary(player2, List.of(existingTop));

        cast(new int[]{0, 1}, List.of(creature.getId(), aura.getId()));
        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creature.getCard(), existingTop);
        harness.assertInHand(player1, "Bind the Monster");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura.getCard());
        harness.assertNotOnBattlefield(player1, "Bind the Monster");
        harness.assertInGraveyard(player1, "Run Ashore");
    }

    @Test
    @CardUsed({RunAshore.class, Island.class})
    void secondModeCannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new RunAshore()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{1}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @CardUsed({RunAshore.class, CravenHulk.class, Island.class})
    void legalSecondTargetStillReturnsWhenFirstTargetLeavesBeforeResolution() {
        Permanent libraryTarget = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        Permanent handTarget = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        Card existingTop = new Island();
        harness.setLibrary(player2, List.of(existingTop));
        harness.setHand(player1, List.of(new RunAshore()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(libraryTarget.getId(), handTarget.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(libraryTarget);
        gd.playerGraveyards.get(player2.getId()).add(libraryTarget.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTop);
        assertThat(gd.playerHands.get(player2.getId())).contains(handTarget.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(libraryTarget.getCard());
        harness.assertInGraveyard(player1, "Run Ashore");
    }

    @Test
    @CardUsed({RunAshore.class, CravenHulk.class, Island.class})
    void legalFirstTargetStillMovesWhenSecondTargetLeavesBeforeResolution() {
        Permanent libraryTarget = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        Permanent handTarget = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        Card existingTop = new Island();
        harness.setLibrary(player2, List.of(existingTop));
        harness.setHand(player1, List.of(new RunAshore()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(libraryTarget.getId(), handTarget.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(handTarget);
        gd.playerGraveyards.get(player2.getId()).add(handTarget.getCard());

        harness.passBothPriorities();
        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryTarget.getCard(), existingTop);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(handTarget.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(handTarget.getCard());
        harness.assertInGraveyard(player1, "Run Ashore");
    }

    private void cast(int[] modes, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new RunAshore()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 6);
    }

}
