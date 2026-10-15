package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DimirGuildgate;
import com.github.laxika.magicalvibes.cards.b.BartizanBats;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnexplainedDisappearance.class, BartizanBats.class, DimirGuildgate.class})
class UnexplainedDisappearanceTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the target creature and surveils 1")
    void returnsTargetCreatureAndSurveils() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BartizanBats()).getId();
        Card topCard = new BartizanBats();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new UnexplainedDisappearance()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleMayAbilityChosen(player1, true);

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Bartizan Bats");
        harness.assertInHand(player2, "Bartizan Bats");
        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gameData.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Leaves the top card on the library when surveil is declined")
    void declinesSurveil() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BartizanBats()).getId();
        Card topCard = new BartizanBats();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new UnexplainedDisappearance()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleMayAbilityChosen(player1, false);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gameData.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new BartizanBats());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DimirGuildgate()).getId();
        harness.setHand(player1, List.of(new UnexplainedDisappearance()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void emptyLibraryDoesNotPreventReturningOwnCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new BartizanBats()).getId();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new UnexplainedDisappearance()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Bartizan Bats");
        harness.assertInHand(player1, "Bartizan Bats");
        harness.assertInGraveyard(player1, "Unexplained Disappearance");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void returnsStolenCreatureToOwnerAndSurveilsCastersLibrary() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new BartizanBats()).getId();
        gd.stolenCreatures.put(targetId, player2.getId());
        Card topCard = new DimirGuildgate();
        Card secondCard = new BartizanBats();
        Card opponentsTopCard = new BartizanBats();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setLibrary(player2, List.of(opponentsTopCard));
        harness.setHand(player1, List.of(new UnexplainedDisappearance()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.assertNotOnBattlefield(player1, "Bartizan Bats");
        harness.assertInHand(player2, "Bartizan Bats");
        harness.assertNotInHand(player1, "Bartizan Bats");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsTopCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentsTopCard);
    }

    @Test
    void doesNotSurveilWhenOnlyTargetLeavesBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BartizanBats()).getId();
        Card topCard = new DimirGuildgate();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new UnexplainedDisappearance()));
        harness.setHand(player2, List.of(new UnexplainedDisappearance()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Bartizan Bats");
        harness.assertInGraveyard(player1, "Unexplained Disappearance");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
