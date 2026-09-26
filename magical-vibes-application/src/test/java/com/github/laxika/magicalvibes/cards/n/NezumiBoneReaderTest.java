package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NezumiBoneReader.class, WanderingOnes.class})
class NezumiBoneReaderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature makes the target player discard a card")
    void targetPlayerDiscards() {
        setupBoneReader();
        harness.addToBattlefield(player1, new WanderingOnes());
        UUID wanderingOnesId = harness.getPermanentId(player1, "Wandering Ones");
        harness.setHand(player2, List.of(new WanderingOnes()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, wanderingOnesId);
        harness.assertInGraveyard(player1, "Wandering Ones");

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Wandering Ones");
    }

    @Test
    @DisplayName("Nezumi Bone-Reader can be sacrificed to its own ability")
    void canSacrificeItself() {
        setupBoneReader();
        harness.setHand(player2, List.of(new WanderingOnes()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertNotOnBattlefield(player1, "Nezumi Bone-Reader");
        harness.assertInGraveyard(player1, "Nezumi Bone-Reader");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller can be targeted too")
    void canTargetController() {
        setupBoneReader();
        harness.setHand(player1, List.of(new WanderingOnes()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target only a player")
    void cannotTargetPermanent() {
        setupBoneReader();
        harness.addToBattlefield(player2, new WanderingOnes());
        UUID wanderingOnesId = harness.getPermanentId(player2, "Wandering Ones");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wanderingOnesId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activates only as a sorcery")
    void cannotActivateAtInstantSpeed() {
        setupBoneReader();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires {B} to activate")
    void requiresMana() {
        addCreatureReady(player1, new NezumiBoneReader());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupBoneReader() {
        addCreatureReady(player1, new NezumiBoneReader());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
