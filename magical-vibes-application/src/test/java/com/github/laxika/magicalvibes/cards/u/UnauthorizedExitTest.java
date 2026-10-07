package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CaseOfTheFilchedFalcon;
import com.github.laxika.magicalvibes.cards.g.GraniteWitness;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnauthorizedExit.class, CaseOfTheFilchedFalcon.class, GraniteWitness.class, Island.class})
class UnauthorizedExitTest extends BaseCardTest {

    @Test
    void returnsTargetNonlandPermanentAndSurveilsOne() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new CaseOfTheFilchedFalcon()).getId();
        Card topCard = new GraniteWitness();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new UnauthorizedExit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleMayAbilityChosen(player1, true);

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Case of the Filched Falcon");
        harness.assertInHand(player2, "Case of the Filched Falcon");
        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gameData.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void cannotTargetLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Island()).getId();
        harness.setHand(player1, List.of(new UnauthorizedExit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    void canReturnOwnCreatureAndKeepSurveilledCardOnTop() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new GraniteWitness()).getId();
        Card topCard = new Island();
        Card secondCard = new GraniteWitness();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new UnauthorizedExit()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.assertInHand(player1, "Granite Witness");
        harness.assertNotOnBattlefield(player1, "Granite Witness");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryDoesNotPreventReturningPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GraniteWitness()).getId();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new UnauthorizedExit()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInHand(player2, "Granite Witness");
        harness.assertNotOnBattlefield(player2, "Granite Witness");
        harness.assertInGraveyard(player1, "Unauthorized Exit");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void returnsStolenPermanentToOwnerRatherThanController() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new GraniteWitness()).getId();
        gd.stolenCreatures.put(targetId, player2.getId());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new UnauthorizedExit()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Granite Witness");
        harness.assertInHand(player2, "Granite Witness");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotSurveilWhenOnlyTargetLeavesBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GraniteWitness()).getId();
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new UnauthorizedExit()));
        harness.setHand(player2, List.of(new UnauthorizedExit()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Granite Witness");
        harness.assertInGraveyard(player1, "Unauthorized Exit");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
