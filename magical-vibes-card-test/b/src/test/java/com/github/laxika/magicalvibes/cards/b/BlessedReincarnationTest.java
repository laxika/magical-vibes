package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlessedReincarnation.class, FountainOfYouth.class, GrizzlyBears.class, LlanowarElves.class,
        CosisTrickster.class})
class BlessedReincarnationTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opposing creature and replaces it with the first creature from its controller's library")
    void exilesTargetAndReplacesItWithRevealedCreature() {
        harness.addToBattlefield(player2, new LlanowarElves());
        BlessedReincarnation card = new BlessedReincarnation();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        GameData gameData = harness.getGameData();
        harness.setLibrary(player2, List.of(new FountainOfYouth(), new GrizzlyBears()));

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .anyMatch(exiled -> exiled.getName().equals("Llanowar Elves"));
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gameData.playerDecks.get(player2.getId()))
                .anyMatch(remaining -> remaining.getName().equals("Fountain of Youth"));
        assertThat(gameData.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    @DisplayName("Shuffles the revealed cards back when the opponent's library has no creature")
    void shufflesRevealedCardsBackWhenNoCreatureIsFound() {
        harness.addToBattlefield(player2, new LlanowarElves());
        BlessedReincarnation card = new BlessedReincarnation();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        GameData gameData = harness.getGameData();
        harness.setLibrary(player2, List.of(new FountainOfYouth()));

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .anyMatch(exiled -> exiled.getName().equals("Llanowar Elves"));
        assertThat(gameData.playerDecks.get(player2.getId()))
                .extracting("name").containsExactly("Fountain of Youth");
        assertThat(gameData.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the spell's controller")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new BlessedReincarnation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    void reboundReplacesAnotherCreatureWithoutPayingMana() {
        var target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        BlessedReincarnation card = new BlessedReincarnation();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Blessed Reincarnation");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void decliningReboundLeavesSpellExiledAndCreatureUntouched() {
        var target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        BlessedReincarnation card = new BlessedReincarnation();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());

        advanceToUpkeep(player2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void removedTargetPreventsReplacementAndRebound() {
        var target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        BlessedReincarnation card = new BlessedReincarnation();
        GrizzlyBears replacement = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player2, List.of(replacement));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(replacement);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Blessed Reincarnation");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    @CardUsed({BlessedReincarnation.class, CosisTrickster.class, LlanowarElves.class})
    void emptyLibraryStillTriggersShuffleAbilities() {
        harness.addToBattlefield(player1, new CosisTrickster());
        var target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new BlessedReincarnation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Cosi's Trickster"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }
}
