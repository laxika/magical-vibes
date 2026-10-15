package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TonicPeddler.class, Forest.class})
class TonicPeddlerTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card and paying white mana makes target player gain 3 life")
    void gainsLifeForTargetPlayer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent peddler = addCreatureReady(player1, new TonicPeddler());
        harness.setHand(player1, List.of(new Forest()));
        int initialLife = gd.playerLifeTotals.get(player2.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(initialLife + 3);
        assertThat(peddler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target its controller")
    void gainsLifeForController() {
        Permanent peddler = addCreatureReady(player1, new TonicPeddler());
        harness.setHand(player1, List.of(new Forest()));
        int initialLife = gd.playerLifeTotals.get(player1.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, initialLife + 3);
        assertThat(peddler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        addCreatureReady(player1, new TonicPeddler());
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new TonicPeddler());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent peddler = addCreatureReady(player1, new TonicPeddler());
        peddler.tap();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Green mana cannot pay the white activation cost")
    void cannotActivateWithoutWhiteMana() {
        Permanent peddler = addCreatureReady(player1, new TonicPeddler());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Forest");
        assertThat(peddler.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Costs are paid before life gain, and ability resolves without its source")
    void costsPaidBeforeResolutionAndSourceIsNotRequired() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        Permanent peddler = addCreatureReady(player1, new TonicPeddler());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player2, 10);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
        assertThat(peddler.isTapped()).isTrue();
        harness.assertLife(player2, 10);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(peddler);
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
        assertThat(gd.stack).isEmpty();
    }
}
