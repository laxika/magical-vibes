package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistmeadowWitch.class, Island.class})
class MistmeadowWitchTest extends BaseCardTest {

    private void addReadyWitch(Player player) {
        addCreatureReady(player, new MistmeadowWitch());
    }

    private void addWitchMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Ability exiles the target creature and schedules its return")
    void exilesTargetCreature() {
        addReadyWitch(player1);
        harness.addToBattlefield(player2, new MistmeadowWitch());
        addWitchMana(player1);

        UUID creatureId = harness.getPermanentId(player2, "Mistmeadow Witch");
        harness.activateAbility(player1, 0, 0, null, creatureId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mistmeadow Witch");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mistmeadow Witch"));
        assertThat(gd.getDelayedActions(PendingExileReturn.class))
                .anyMatch(per -> per.card().getName().equals("Mistmeadow Witch"));
    }

    @Test
    @DisplayName("Exiled creature returns at the next end step under its owner's control")
    void returnsAtEndStep() {
        addReadyWitch(player1);
        harness.addToBattlefield(player2, new MistmeadowWitch());
        addWitchMana(player1);

        UUID creatureId = harness.getPermanentId(player2, "Mistmeadow Witch");
        harness.activateAbility(player1, 0, 0, null, creatureId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mistmeadow Witch");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mistmeadow Witch");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Mistmeadow Witch"));
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyWitch(player1);
        harness.addToBattlefield(player2, new Island());
        addWitchMana(player1);

        UUID islandId = harness.getPermanentId(player2, "Island");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, islandId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick Witch can exile itself and return as a new permanent")
    void summoningSickWitchCanExileItself() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new MistmeadowWitch());
        addWitchMana(player1);

        harness.activateAbility(player1, 0, 0, null, witch.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Mistmeadow Witch");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Mistmeadow Witch");
        assertThat(returned.getId()).isNotEqualTo(witch.getId());
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A stolen creature returns to its owner rather than its previous controller")
    void stolenCreatureReturnsToOwner() {
        addReadyWitch(player1);
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new MistmeadowWitch());
        gd.stolenCreatures.put(stolen.getId(), player1.getId());
        addWitchMana(player1);

        harness.activateAbility(player1, 0, 0, null, stolen.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Mistmeadow Witch")).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Mistmeadow Witch");
    }

    @Test
    @DisplayName("Exiling during an end step waits until the following turn's end step")
    void activationDuringEndStepWaitsForNextEndStep() {
        harness.addToBattlefield(player1, new MistmeadowWitch());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        addWitchMana(player1);
        UUID witchId = harness.getPermanentId(player1, "Mistmeadow Witch");

        harness.activateAbility(player1, 0, 0, null, witchId);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Mistmeadow Witch");

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.assertNotOnBattlefield(player1, "Mistmeadow Witch");
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mistmeadow Witch");
    }

    @Test
    @DisplayName("The delayed return cannot track a card that left exile and was exiled again")
    void doesNotReturnNewExileObjectBeforeTrigger() {
        harness.addToBattlefield(player1, new MistmeadowWitch());
        MistmeadowWitch target = new MistmeadowWitch();
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, target);
        addWitchMana(player1);

        harness.activateAbility(player1, 0, 0, null, permanent.getId());
        harness.passBothPriorities();

        // Model an intervening move out of exile followed by an unrelated exile.
        harness.inMutationScope(() -> {
            assertThat(gd.removeFromExile(target.getId())).isTrue();
            gd.playerGraveyards.get(player2.getId()).add(target);
            gd.playerGraveyards.get(player2.getId()).remove(target);
            gd.addToExile(player2.getId(), target);
        });

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mistmeadow Witch");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
    }
}
