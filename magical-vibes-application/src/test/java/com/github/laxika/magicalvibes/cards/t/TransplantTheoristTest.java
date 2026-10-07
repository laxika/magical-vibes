package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.p.ProstheticInjector;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TransplantTheorist.class, Forest.class, CopperLonglegs.class, ProstheticInjector.class})
class TransplantTheoristTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldTriggersLoot() {
        harness.setHand(player1, List.of(new TransplantTheorist()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    void mayLootCanBeDeclined() {
        harness.setHand(player1, List.of(new TransplantTheorist()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void anotherArtifactEnteringTheBattlefieldTriggersLoot() {
        harness.addToBattlefield(player1, new TransplantTheorist());
        harness.setHand(player1, List.of(new ProstheticInjector()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    void putsTargetGraveyardCardOnBottomOfLibrary() {
        Permanent theorist = harness.addToBattlefieldAndReturn(player1, new TransplantTheorist());
        Card target = new CopperLonglegs();
        Card libraryCard = new Forest();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int theoristIndex = gd.playerBattlefields.get(player1.getId()).indexOf(theorist);
        harness.activateAbilityWithGraveyardTargets(player1, theoristIndex, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(libraryCard.getId(), target.getId());
    }

    @Test
    void cannotTargetAnOpponentGraveyardCard() {
        Permanent theorist = harness.addToBattlefieldAndReturn(player1, new TransplantTheorist());
        Card target = new CopperLonglegs();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int theoristIndex = gd.playerBattlefields.get(player1.getId()).indexOf(theorist);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, theoristIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonartifactCreatureEnteringDoesNotTriggerLoot() {
        harness.addToBattlefield(player1, new TransplantTheorist());
        harness.setHand(player1, List.of(new CopperLonglegs()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsArtifactEnteringDoesNotTriggerLoot() {
        harness.addToBattlefield(player2, new TransplantTheorist());
        harness.setHand(player1, List.of(new ProstheticInjector()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canDiscardAnExistingHandCardInsteadOfTheDrawnCard() {
        harness.setHand(player1, List.of(new TransplantTheorist(), new CopperLonglegs()));
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Copper Longlegs");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canActivateTwiceWhileSummoningSickToRecycleDifferentCardTypes() {
        harness.addToBattlefield(player1, new TransplantTheorist());
        Card creature = new CopperLonglegs();
        Card land = new Forest();
        Card libraryCard = new ProstheticInjector();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(libraryCard.getId(), creature.getId(), land.getId());
    }

    @Test
    void targetLeavingGraveyardBeforeResolutionIsNotMovedAgain() {
        harness.addToBattlefield(player1, new TransplantTheorist());
        Card target = new CopperLonglegs();
        Card remaining = new Forest();
        Card libraryCard = new ProstheticInjector();
        harness.setGraveyard(player1, List.of(target, remaining));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
    }
}
