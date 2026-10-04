package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlameJet.class, MetathranSoldier.class, GarrukWildspeaker.class})
class FlameJetTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FlameJet()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @CardUsed(GarrukWildspeaker.class)
    void dealsThreeDamageToTargetPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new FlameJet()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MetathranSoldier());
        harness.setHand(player1, List.of(new FlameJet()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cyclingDiscardsTheCardAndDrawsOne() {
        harness.setHand(player1, List.of(new FlameJet()));
        harness.setLibrary(player1, List.of(new MetathranSoldier()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Flame Jet");
        harness.assertInHand(player1, "Metathran Soldier");
    }

    @Test
    void canTargetItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FlameJet()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Flame Jet");
    }

    @Test
    void cyclingDiscardsImmediatelyButDrawsOnlyOnResolution() {
        harness.setHand(player1, List.of(new FlameJet()));
        harness.setLibrary(player1, List.of(new MetathranSoldier()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Flame Jet");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Metathran Soldier");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotCycleWithoutPayingTwoMana() {
        harness.setHand(player1, List.of(new FlameJet()));
        harness.setLibrary(player1, List.of(new MetathranSoldier()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Flame Jet");
        harness.assertNotInGraveyard(player1, "Flame Jet");
        harness.assertNotInHand(player1, "Metathran Soldier");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCycleDuringOpponentsTurnWithoutDealingDamage() {
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FlameJet()));
        harness.setLibrary(player1, List.of(new MetathranSoldier()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Flame Jet");
        harness.assertInHand(player1, "Metathran Soldier");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
