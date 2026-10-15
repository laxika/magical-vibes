package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HostileDesert;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SterlingKeykeeper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cactarantula.class, HostileDesert.class, Shock.class, SterlingKeykeeper.class})
class CactarantulaTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast without a Desert for only five mana")
    void cannotCastWithoutDesert() {
        harness.setHand(player1, List.of(new Cactarantula()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Costs one less to cast while controlling a Desert")
    void costsOneLessWithDesert() {
        harness.addToBattlefield(player1, new HostileDesert());
        harness.castFromHand(player1, new Cactarantula(), "{G}{G}{G}{G}{G}");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("May draw when targeted by an opponent's spell")
    void mayDrawWhenTargetedByOpponentSpell() {
        harness.addToBattlefield(player1, new Cactarantula());
        UUID cactarantulaId = harness.getPermanentId(player1, "Cactarantula");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, cactarantulaId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Can cast for six mana without a Desert")
    void castsForFullCostWithoutDesert() {
        harness.castFromHand(player1, new Cactarantula(), "{4}{G}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cactarantula");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("An opponent's Desert does not reduce the cost")
    void opponentDesertDoesNotReduceCost() {
        harness.addToBattlefield(player2, new HostileDesert());
        harness.setHand(player1, List.of(new Cactarantula()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Multiple Deserts still reduce the cost by only one")
    void multipleDesertsDoNotIncreaseReduction() {
        harness.addToBattlefield(player1, new HostileDesert());
        harness.addToBattlefield(player1, new HostileDesert());
        harness.setHand(player1, List.of(new Cactarantula()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The Desert discount does not remove a green mana requirement")
    void discountStillRequiresTwoGreenMana() {
        harness.addToBattlefield(player1, new HostileDesert());
        harness.setHand(player1, List.of(new Cactarantula()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("May decline the draw without countering the opponent's spell")
    void mayDeclineDraw() {
        Permanent cactarantula = harness.addToBattlefieldAndReturn(player1, new Cactarantula());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, cactarantula.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(cactarantula.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Targeting Cactarantula with its controller's spell does not trigger a draw")
    void ownSpellDoesNotTrigger() {
        Permanent cactarantula = harness.addToBattlefieldAndReturn(player1, new Cactarantula());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, cactarantula.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(cactarantula.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Draw resolves before an opponent's activated ability taps Cactarantula")
    void opponentActivatedAbilityTriggersDraw() {
        Permanent cactarantula = harness.addToBattlefieldAndReturn(player1, new Cactarantula());
        addCreatureReady(player2, new SterlingKeykeeper());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Cactarantula()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, cactarantula.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(cactarantula.isTapped()).isFalse();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Cactarantula");
        resolveAllTriggers();
        assertThat(cactarantula.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Its controller's activated ability does not trigger the draw")
    void ownActivatedAbilityDoesNotTrigger() {
        addCreatureReady(player1, new SterlingKeykeeper());
        Permanent cactarantula = harness.addToBattlefieldAndReturn(player1, new Cactarantula());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, cactarantula.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(cactarantula.isTapped()).isTrue();
    }
}
