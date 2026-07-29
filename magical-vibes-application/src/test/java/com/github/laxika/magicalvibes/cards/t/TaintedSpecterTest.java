package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaintedSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Declining the library option discards a card and blasts everything for 1")
    void discardDealsOneDamageToEachCreatureAndPlayer() {
        setupSpecter();
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player1, "Tainted Specter");
    }

    @Test
    @DisplayName("Putting a card on top of the library avoids the discard and the damage")
    void puttingCardOnLibraryDealsNoDamage() {
        setupSpecter();
        harness.addToBattlefield(player2, new FugitiveWizard());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        harness.handleMultipleCardsChosen(player2, List.of(bears.getId()));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("An empty hand means no discard, so no damage is dealt")
    void emptyHandDealsNoDamage() {
        setupSpecter();
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
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
        harness.addToBattlefield(player1, new TaintedSpecter());
        findPermanent(player1, "Tainted Specter").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 3);
    }
}
