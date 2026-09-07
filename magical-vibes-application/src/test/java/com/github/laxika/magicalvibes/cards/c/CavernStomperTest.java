package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CavernStomper.class, GrizzlyBears.class, HillGiant.class})
class CavernStomperTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield offers scry 2")
    void etbOffersScryTwo() {
        harness.setHand(player1, List.of(new CavernStomper()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Activated ability prevents power 2 or less creatures from blocking this turn")
    void activatedAbilityRestrictsBlockersByPower() {
        Permanent stomper = addCreatureReady(player1, new CavernStomper());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        attack(stomper);
        assertThatThrownBy(() -> declareBlock(bears, stomper))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or greater");
    }

    @Test
    @DisplayName("Activated ability still allows a creature with power 3 or greater to block")
    void activatedAbilityAllowsLargerBlockers() {
        Permanent stomper = addCreatureReady(player1, new CavernStomper());
        Permanent hillGiant = addCreatureReady(player2, new HillGiant());

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        attack(stomper);
        declareBlock(hillGiant, stomper);

        assertThat(hillGiant.isBlocking()).isTrue();
    }

    private void attack(Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
