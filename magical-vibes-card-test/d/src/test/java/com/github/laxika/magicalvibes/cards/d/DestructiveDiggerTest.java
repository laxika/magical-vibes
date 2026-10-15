package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DestructiveDigger.class, Forest.class, GrizzlyBears.class, LeoninScimitar.class})
class DestructiveDiggerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact draws a card")
    void sacrificingArtifactDrawsCard() {
        addCreatureReady(player1, new DestructiveDigger());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Sacrificing a land draws a card")
    void sacrificingLandDrawsCard() {
        addCreatureReady(player1, new DestructiveDigger());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate without an artifact or land to sacrifice")
    void cannotActivateWithoutArtifactOrLand() {
        addCreatureReady(player1, new DestructiveDigger());
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact or land");
    }

    @Test
    @DisplayName("Tap and sacrifice costs are paid before the card is drawn")
    void paysCostsBeforeDrawingAndCanSacrificeTappedLand() {
        Permanent digger = addCreatureReady(player1, new DestructiveDigger());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);

        assertThat(digger.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Controller chooses one permanent when both an artifact and a land are available")
    void choosesOnePermanentToSacrifice() {
        addCreatureReady(player1, new DestructiveDigger());
        harness.addToBattlefield(player1, new LeoninScimitar());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Leonin Scimitar");
        harness.assertNotInGraveyard(player1, "Leonin Scimitar");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent digger = addCreatureReady(player1, new DestructiveDigger());
        digger.setSummoningSick(true);
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(digger.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent digger = addCreatureReady(player1, new DestructiveDigger());
        digger.tap();
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with only two mana")
    void cannotActivateWithoutThreeMana() {
        Permanent digger = addCreatureReady(player1, new DestructiveDigger());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(digger.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponents' artifacts and lands cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsPermanents() {
        addCreatureReady(player1, new DestructiveDigger());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact or land");

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature that is neither an artifact nor a land cannot pay the sacrifice cost")
    void cannotSacrificeOrdinaryCreature() {
        addCreatureReady(player1, new DestructiveDigger());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact or land");

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
