package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FearOfLostTeeth;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValgavothsFaithful.class, FearOfLostTeeth.class, Mountain.class})
class ValgavothsFaithfulTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and returns a creature from the graveyard")
    void sacrificesItselfAndReturnsCreature() {
        Card creature = new FearOfLostTeeth();
        harness.addToBattlefield(player1, new ValgavothsFaithful());
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana(player1);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Valgavoth's Faithful");
        harness.assertOnBattlefield(player1, "Fear of Lost Teeth");
        harness.assertNotInGraveyard(player1, "Fear of Lost Teeth");
        harness.assertInGraveyard(player1, "Valgavoth's Faithful");
    }

    @Test
    @DisplayName("Requires a creature card as the graveyard target")
    void rejectsNonCreatureGraveyardTarget() {
        Card land = new Mountain();
        harness.addToBattlefield(player1, new ValgavothsFaithful());
        harness.setGraveyard(player1, List.of(land));
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Valgavoth's Faithful");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        harness.addToBattlefield(player1, new ValgavothsFaithful());
        Card creature = new FearOfLostTeeth();
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsOpponentsGraveyard() {
        Card creature = new FearOfLostTeeth();
        harness.addToBattlefield(player1, new ValgavothsFaithful());
        harness.setGraveyard(player2, List.of(creature));
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Valgavoth's Faithful");
        harness.assertInGraveyard(player2, "Fear of Lost Teeth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetItselfBeforePayingSacrificeCost() {
        Card faithful = new ValgavothsFaithful();
        harness.addToBattlefield(player1, faithful);
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, faithful.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Valgavoth's Faithful");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateWhileTappedAndSummoningSickAndPaysSacrificeImmediately() {
        Permanent faithful = harness.addToBattlefieldAndReturn(player1, new ValgavothsFaithful());
        faithful.tap();
        faithful.setSummoningSick(true);
        Card creature = new FearOfLostTeeth();
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana(player1);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);

        harness.assertNotOnBattlefield(player1, "Valgavoth's Faithful");
        harness.assertInGraveyard(player1, "Valgavoth's Faithful");
        harness.assertInGraveyard(player1, "Fear of Lost Teeth");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fear of Lost Teeth");
        assertThat(findPermanent(player1, "Fear of Lost Teeth").isTapped()).isFalse();
    }

    @Test
    void doesNotReturnAnotherCreatureIfTargetLeavesGraveyard() {
        Card creature = new FearOfLostTeeth();
        harness.addToBattlefield(player1, new ValgavothsFaithful());
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana(player1);
        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);

        gd.playerGraveyards.get(player1.getId()).remove(creature);
        gd.playerHands.get(player1.getId()).add(creature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fear of Lost Teeth");
        harness.assertInHand(player1, "Fear of Lost Teeth");
        harness.assertInGraveyard(player1, "Valgavoth's Faithful");
        harness.assertNotOnBattlefield(player1, "Valgavoth's Faithful");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsActivationOutsideMainPhase() {
        Card creature = new FearOfLostTeeth();
        harness.addToBattlefield(player1, new ValgavothsFaithful());
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Valgavoth's Faithful");
    }

    @Test
    void rejectsActivationWithNonemptyStack() {
        Card creature = new FearOfLostTeeth();
        harness.addToBattlefield(player1, new ValgavothsFaithful());
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, new ValgavothsFaithful(), "{B}");
        addActivationMana(player1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Valgavoth's Faithful");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void requiresBlackManaInAdditionToGenericMana() {
        Card creature = new FearOfLostTeeth();
        harness.addToBattlefield(player1, new ValgavothsFaithful());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Valgavoth's Faithful");
        harness.assertInGraveyard(player1, "Fear of Lost Teeth");
        assertThat(gd.stack).isEmpty();
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
