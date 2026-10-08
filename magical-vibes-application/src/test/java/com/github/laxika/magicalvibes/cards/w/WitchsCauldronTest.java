package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitchsCauldron.class, GrizzlyBears.class})
class WitchsCauldronTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature gains 1 life and draws a card")
    void sacrificeCreatureGainsLifeAndDrawsCard() {
        harness.addToBattlefield(player1, new WitchsCauldron());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        GameData gameData = harness.getGameData();
        int handSizeBefore = gameData.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Witch's Cauldron cannot be activated without a creature to sacrifice")
    void requiresCreatureToSacrifice() {
        harness.addToBattlefield(player1, new WitchsCauldron());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void costsArePaidBeforeEffectsResolve() {
        var cauldron = harness.addToBattlefieldAndReturn(player1, new WitchsCauldron());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        GameData gameData = harness.getGameData();
        int handSize = gameData.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(cauldron.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.assertLife(player1, 20);
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gameData.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void cannotActivateWhileTapped() {
        var cauldron = harness.addToBattlefieldAndReturn(player1, new WitchsCauldron());
        cauldron.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotPayBlackRequirementWithColorlessMana() {
        harness.addToBattlefield(player1, new WitchsCauldron());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotActivateWithOnlyOneBlackMana() {
        harness.addToBattlefield(player1, new WitchsCauldron());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new WitchsCauldron());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void controllerChoosesExactlyOneCreatureIncludingTappedCreatures() {
        harness.addToBattlefield(player1, new WitchsCauldron());
        var first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        second.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(gameData.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gameData.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }
}
