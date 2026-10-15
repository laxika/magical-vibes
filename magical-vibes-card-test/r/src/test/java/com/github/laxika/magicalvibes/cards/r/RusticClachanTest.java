package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MosquitoGuard;
import com.github.laxika.magicalvibes.cards.m.MothdustChangeling;
import com.github.laxika.magicalvibes.cards.v.VioletPall;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RusticClachan.class, Forest.class, GoldmeadowStalwart.class, GrizzlyBears.class,
        MosquitoGuard.class, MothdustChangeling.class, VioletPall.class})
class RusticClachanTest extends BaseCardTest {

    // ===== Enters tapped / reveal choice =====

    @Test
    @DisplayName("Enters tapped when you have no Kithkin card in hand")
    void entersTappedWithoutKithkin() {
        harness.setHand(player1, List.of(new RusticClachan(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a Kithkin lets it enter untapped")
    void entersUntappedWhenRevealing() {
        harness.setHand(player1, List.of(new RusticClachan(), new GoldmeadowStalwart()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped even with a Kithkin in hand")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new RusticClachan(), new GoldmeadowStalwart()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping produces one white mana")
    void tappingProducesWhiteMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RusticClachan());
        land.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    // ===== Reinforce =====

    @Test
    @DisplayName("Reinforce puts a +1/+1 counter on target creature and discards the source")
    void reinforceBoostsTargetCreature() {
        harness.setHand(player1, List.of(new RusticClachan()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Rustic Clachan");
    }

    @Test
    @DisplayName("Reinforce cannot target a non-creature; the card stays in hand")
    void reinforceRejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new RusticClachan()));
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Rustic Clachan");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A changeling card can be revealed and remains in hand")
    void canRevealChangeling() {
        harness.setHand(player1, List.of(new RusticClachan(), new MothdustChangeling()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
        harness.assertInHand(player1, "Mothdust Changeling");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Kithkin in the opponent's hand cannot be revealed")
    void cannotRevealOpponentsKithkin() {
        harness.setHand(player1, List.of(new RusticClachan()));
        harness.setHand(player2, List.of(new MosquitoGuard()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findLand(player1).isTapped()).isTrue();
        harness.assertInHand(player2, "Mosquito Guard");
    }

    @Test
    @DisplayName("Reinforce can target an opponent's creature and pays costs before resolution")
    void reinforceCanTargetOpponentsCreature() {
        harness.setHand(player1, List.of(new RusticClachan()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MosquitoGuard());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, creature.getId());

        harness.assertNotInHand(player1, "Rustic Clachan");
        harness.assertInGraveyard(player1, "Rustic Clachan");
        assertThat(gd.stack).hasSize(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reinforce requires white mana and does not discard on failed payment")
    void reinforceRequiresWhiteMana() {
        harness.setHand(player1, List.of(new RusticClachan()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MosquitoGuard());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Rustic Clachan");
        harness.assertNotInGraveyard(player1, "Rustic Clachan");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reinforce can be activated during the opponent's upkeep")
    void reinforceAtInstantSpeed() {
        harness.setHand(player1, List.of(new RusticClachan()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MosquitoGuard());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateHandAbility(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Rustic Clachan");
    }


    @Test
    @DisplayName("Reinforce does nothing when its target is destroyed in response")
    void reinforceLosesDestroyedTarget() {
        harness.setHand(player1, List.of(new RusticClachan()));
        harness.setHand(player2, List.of(new VioletPall()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MosquitoGuard());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLACK, 5);

        harness.activateHandAbility(player1, 0, creature.getId());
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mosquito Guard");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Rustic Clachan");
        harness.assertNotInHand(player1, "Rustic Clachan");
    }

    @Test
    @DisplayName("An untapped Rustic Clachan can produce mana immediately after being played")
    void canTapForManaOnEntry() {
        harness.setHand(player1, List.of(new RusticClachan(), new MosquitoGuard()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(findLand(player1).isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
    private Permanent findLand(Player player) {
        return findPermanent(player, "Rustic Clachan");
    }
}
