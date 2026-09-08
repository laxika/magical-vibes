package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StartledAwakeTest extends BaseCardTest {

    @Test
    @DisplayName("Mills thirteen cards from the targeted opponent")
    void millsThirteenCardsFromTargetOpponent() {
        harness.setLibrary(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()
        ));
        harness.setHand(player1, List.of(new StartledAwake()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(13);
    }

    @Test
    @DisplayName("Returns from the graveyard transformed as Persistent Nightmare")
    void returnsFromGraveyardTransformed() {
        harness.setGraveyard(player1, List.of(new StartledAwake()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent nightmare = findPermanent(player1, "Persistent Nightmare");
        assertThat(nightmare.isTransformed()).isTrue();
        harness.assertNotInGraveyard(player1, "Startled Awake");
    }

    @Test
    @DisplayName("Graveyard ability cannot be activated outside sorcery timing")
    void graveyardAbilityRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new StartledAwake()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Persistent Nightmare returns to its owner's hand after combat damage to a player")
    void persistentNightmareReturnsToHandAfterCombatDamage() {
        Permanent nightmare = addCreatureReady(player1, new StartledAwake().getBackFaceCard());
        nightmare.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Persistent Nightmare");
    }
}
