package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoonOfErebos.class, TravelingPhilosopher.class, LightningStrike.class})
class BoonOfErebosTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts and regenerates the target creature, then the spell's controller loses 2 life")
    void boostsRegeneratesAndLosesLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new BoonOfErebos()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getRegenerationShield()).isEqualTo(1);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The regeneration shield saves the target from lethal damage")
    void regenerationShieldSavesTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new BoonOfErebos(), new LightningStrike()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Targeting an opponent's creature still makes the caster lose life")
    void opponentCreatureDoesNotChangeLifeLossRecipient() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new BoonOfErebos()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getRegenerationShield()).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An illegal sole target prevents the life loss too")
    void removedTargetPreventsLifeLoss() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new BoonOfErebos()));
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Traveling Philosopher");
        harness.assertInGraveyard(player1, "Boon of Erebos");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Both the boost and unused regeneration shield expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new BoonOfErebos()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getRegenerationShield()).isZero();
        harness.assertLife(player1, 18);
    }
}
