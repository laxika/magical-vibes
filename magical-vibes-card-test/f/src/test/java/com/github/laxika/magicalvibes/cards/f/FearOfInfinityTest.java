package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FearOfInfinity.class, DazzlingTheaterPropRoom.class, FearOfLostTeeth.class})
class FearOfInfinityTest extends BaseCardTest {

    @Test
    void returnsFromGraveyardAfterAnEnchantmentEnters() {
        FearOfInfinity fear = new FearOfInfinity();
        harness.setGraveyard(player1, List.of(fear));
        harness.setHand(player1, List.of(new FearOfLostTeeth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(fear.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(fear.getId()));
    }

    @Test
    void decliningEnchantmentTriggerKeepsItInGraveyard() {
        FearOfInfinity fear = new FearOfInfinity();
        harness.setGraveyard(player1, List.of(fear));
        harness.setHand(player1, List.of(new FearOfLostTeeth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(fear.getId()));
    }

    @Test
    void returnsFromGraveyardAfterFullyUnlockingARoom() {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        FearOfInfinity fear = new FearOfInfinity();
        harness.setGraveyard(player1, List.of(fear));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(fear.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(fear.getId()));
    }

    @Test
    void cannotBeDeclaredAsABlocker() {
        harness.addToBattlefield(player2, new FearOfInfinity());
        Permanent attacker = addCreatureReady(player1, new FearOfLostTeeth());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentEnchantmentDoesNotTriggerReturn() {
        FearOfInfinity fear = new FearOfInfinity();
        harness.setGraveyard(player1, List.of(fear));

        harness.enterBattlefieldAndReturn(player2, new FearOfLostTeeth());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(fear);
    }

    @Test
    void enchantmentCreatureEnteringTriggersReturnOnlyForTheSourceCard() {
        FearOfInfinity fear = new FearOfInfinity();
        FearOfLostTeeth other = new FearOfLostTeeth();
        harness.setGraveyard(player1, List.of(fear, other));

        harness.enterBattlefieldAndReturn(player1, new FearOfLostTeeth());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(fear);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    void sourceRemovedFromGraveyardBeforeResolutionIsNotReturned() {
        FearOfInfinity fear = new FearOfInfinity();
        harness.setGraveyard(player1, List.of(fear));
        harness.enterBattlefieldAndReturn(player1, new FearOfLostTeeth());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(fear));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(fear);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(fear.getId()));
    }

    @Test
    void decliningFullyUnlockedRoomTriggerKeepsItInGraveyard() {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        FearOfInfinity fear = new FearOfInfinity();
        harness.setGraveyard(player1, List.of(fear));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(fear);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(fear);
    }

    @Test
    void roomEnteringTriggersReturnWithoutBeingFullyUnlocked() {
        FearOfInfinity fear = new FearOfInfinity();
        harness.setGraveyard(player1, List.of(fear));
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(fear);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void combatDamageGainsLife() {
        Permanent fear = addCreatureReady(player1, new FearOfInfinity());
        fear.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void creatureWithoutFlyingOrReachCannotBlockIt() {
        Permanent fear = addCreatureReady(player1, new FearOfInfinity());
        fear.setAttacking(true);
        harness.addToBattlefield(player2, new FearOfLostTeeth());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
