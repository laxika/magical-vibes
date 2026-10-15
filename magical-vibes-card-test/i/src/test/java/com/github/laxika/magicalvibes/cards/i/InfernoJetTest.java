package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.cards.n.NicolBolasGodPharaoh;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({InfernoJet.class, FeralProwler.class, NicolBolasGodPharaoh.class})
class InfernoJetTest extends BaseCardTest {

    private void addManaForInfernoJet() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    @Test
    @DisplayName("Deals 6 damage to target opponent")
    void dealsDamageToOpponent() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new InfernoJet()));
        addManaForInfernoJet();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Cannot target yourself — only an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new InfernoJet()));
        addManaForInfernoJet();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature — only opponent or planeswalker")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FeralProwler());

        harness.setHand(player1, List.of(new InfernoJet()));
        addManaForInfernoJet();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling {2} discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new InfernoJet()));
        harness.setLibrary(player1, List.of(new FeralProwler()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Inferno Jet");
        harness.assertInHand(player1, "Feral Prowler");
    }

    @Test
    @DisplayName("Deals 6 damage to an opponent's planeswalker without damaging its controller")
    void damagesOpponentsPlaneswalker() {
        Permanent bolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasGodPharaoh());
        bolas.setCounterCount(CounterType.LOYALTY, 7);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new InfernoJet()));
        addManaForInfernoJet();

        harness.castAndResolveSorcery(player1, 0, bolas.getId());

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Nicol Bolas, God-Pharaoh");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target your own planeswalker and lethal damage sends it to the graveyard")
    void canDamageOwnPlaneswalker() {
        Permanent bolas = harness.addToBattlefieldAndReturn(player1, new NicolBolasGodPharaoh());
        bolas.setCounterCount(CounterType.LOYALTY, 6);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new InfernoJet()));
        addManaForInfernoJet();

        harness.castAndResolveSorcery(player1, 0, bolas.getId());

        harness.assertNotOnBattlefield(player1, "Nicol Bolas, God-Pharaoh");
        harness.assertInGraveyard(player1, "Nicol Bolas, God-Pharaoh");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cycling on an opponent's turn discards immediately and draws only on resolution")
    void cyclingPaysDiscardBeforeDrawingOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new InfernoJet()));
        harness.setLibrary(player1, List.of(new FeralProwler()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Inferno Jet");
        harness.assertInGraveyard(player1, "Inferno Jet");
        harness.assertNotInHand(player1, "Feral Prowler");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Feral Prowler");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana and does not discard on failure")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new InfernoJet()));
        harness.setLibrary(player1, List.of(new FeralProwler()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Inferno Jet");
        harness.assertNotInHand(player1, "Feral Prowler");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
