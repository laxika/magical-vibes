package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChitteringHost;
import com.github.laxika.magicalvibes.cards.c.ChromeCat;
import com.github.laxika.magicalvibes.cards.g.GrafRats;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MidnightScavengers;
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

@CardUsed({RunOutOfTown.class, GrizzlyBears.class, Island.class, ChromeCat.class,
        ChitteringHost.class, GrafRats.class, MidnightScavengers.class})
class RunOutOfTownTest extends BaseCardTest {

    @Test
    @DisplayName("The target's owner can keep it on top of their library")
    void targetOwnerChoosesTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        cast(target);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player2.getId());

        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), topCard);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Run Out of Town");
    }

    @Test
    @DisplayName("The target's owner can put it on the bottom of their library")
    void targetOwnerChoosesBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        cast(target);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, target.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new RunOutOfTown()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetOwnArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ChromeCat());
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));

        cast(target);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, target.getCard());
        harness.assertNotOnBattlefield(player1, "Chrome Cat");
        harness.assertInGraveyard(player1, "Run Out of Town");
    }

    @Test
    void ownerChoosesDestinationForPermanentControlledByOpponent() {
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        cast(target);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player2.getId());
        assertThatThrownBy(() -> harness.handleListChoice(player1, "Bottom"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, creature);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void bottomChoiceWorksWithEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChromeCat());
        harness.setLibrary(player2, List.of());

        cast(target);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard());
        harness.assertNotOnBattlefield(player2, "Chrome Cat");
    }

    @Test
    void targetRemovedBeforeResolutionDoesNotPromptForDestination() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new RunOutOfTown()));
        addMana();
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Run Out of Town");
    }

    @Test
    void meldedPermanentOwnerCanPutBothComponentsOnBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChitteringHost());
        Card rats = new GrafRats();
        Card scavengers = new MidnightScavengers();
        target.getMeldComponentCards().addAll(List.of(rats, scavengers));
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        cast(target);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, "Bottom");

        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library).hasSize(3);
        assertThat(library.getFirst()).isSameAs(topCard);
        assertThat(library.subList(1, 3)).containsExactlyInAnyOrder(rats, scavengers);
        harness.assertNotOnBattlefield(player2, "Chittering Host");
        harness.assertInGraveyard(player1, "Run Out of Town");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new RunOutOfTown()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
