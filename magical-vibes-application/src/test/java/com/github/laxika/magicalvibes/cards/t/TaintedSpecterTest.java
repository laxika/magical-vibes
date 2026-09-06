package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.n.NobleElephant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TaintedSpecter.class, BayFalcon.class, NobleElephant.class})
class TaintedSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Declining the library option discards a card and blasts everything for 1")
    void discardDealsOneDamageToEachCreatureAndPlayer() {
        setupSpecter();
        harness.addToBattlefield(player2, new BayFalcon());
        harness.setHand(player2, List.of(new NobleElephant()));

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(findPermanent(player1, "Tainted Specter").isTapped()).isTrue();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Noble Elephant");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player2, "Bay Falcon");
        harness.assertOnBattlefield(player1, "Tainted Specter");
    }

    @Test
    @DisplayName("Putting a card on top of the library avoids the discard and the damage")
    void puttingCardOnLibraryDealsNoDamage() {
        setupSpecter();
        harness.addToBattlefield(player2, new BayFalcon());
        NobleElephant elephant = new NobleElephant();
        harness.setHand(player2, List.of(elephant));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        harness.handleMultipleCardsChosen(player2, List.of(elephant.getId()));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Noble Elephant");
        harness.assertNotInGraveyard(player2, "Noble Elephant");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Bay Falcon");
    }

    @Test
    @DisplayName("An empty hand means no discard, so no damage is dealt")
    void emptyHandDealsNoDamage() {
        setupSpecter();
        harness.addToBattlefield(player2, new BayFalcon());
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Bay Falcon");
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetItsController() {
        setupSpecter();
        harness.addToBattlefield(player2, new BayFalcon());
        harness.setHand(player1, List.of(new NobleElephant()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Noble Elephant");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player2, "Bay Falcon");
        harness.assertOnBattlefield(player1, "Tainted Specter");
    }

    @Test
    @DisplayName("Activates only as a sorcery")
    void cannotActivateAtInstantSpeed() {
        setupSpecter();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupSpecter() {
        addCreatureReady(player1, new TaintedSpecter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 3);
    }
}
