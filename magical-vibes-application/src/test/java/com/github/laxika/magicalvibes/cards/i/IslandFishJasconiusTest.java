package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IslandFishJasconiusTest extends BaseCardTest {

    // ===== State trigger: sacrifice when you control no Islands =====

    @Test
    @DisplayName("Sacrificed when controller controls no Islands")
    void sacrificedWhenNoIslands() {
        harness.setHand(player1, List.of(new IslandFishJasconius()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → state trigger fires
        harness.passBothPriorities(); // resolve state trigger → sacrificed

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Island Fish Jasconius"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Island Fish Jasconius"));
    }

    @Test
    @DisplayName("Survives while controller controls an Island")
    void survivesWithIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new IslandFishJasconius()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Island Fish Jasconius"));
    }

    // ===== Doesn't untap during untap step =====

    @Test
    @DisplayName("Tapped Island Fish Jasconius does not untap during controller's untap step")
    void doesNotUntapDuringUntapStep() {
        harness.addToBattlefield(player1, new Island()); // keep the fish alive
        Permanent fish = addFish(player1, true);

        advanceToNextTurn(player2);

        assertThat(fish.isTapped()).isTrue();
    }

    // ===== Upkeep: may pay {U}{U}{U} to untap =====

    @Test
    @DisplayName("Paying {U}{U}{U} during upkeep untaps Island Fish Jasconius")
    void payingUntapsFish() {
        harness.addToBattlefield(player1, new Island()); // keep the fish alive
        Permanent fish = addFish(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.passBothPriorities(); // resolve MayPayManaEffect from stack
        harness.handleMayAbilityChosen(player1, true);

        assertThat(fish.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the upkeep payment leaves Island Fish Jasconius tapped")
    void decliningLeavesFishTapped() {
        harness.addToBattlefield(player1, new Island()); // keep the fish alive
        Permanent fish = addFish(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(fish.isTapped()).isTrue();
    }

    // ===== Attack restriction: defending player must control an Island =====

    @Test
    @DisplayName("Can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Island()); // keep the fish alive
        harness.addToBattlefield(player2, new Island());

        Permanent fish = addFish(player1, false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int fishIndex = gd.playerBattlefields.get(player1.getId()).indexOf(fish);
        gs.declareAttackers(gd, player1, List.of(fishIndex));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Island")
    void cannotAttackWhenDefenderHasNoIsland() {
        harness.addToBattlefield(player1, new Island()); // keep the fish alive

        Permanent fish = addFish(player1, false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int fishIndex = gd.playerBattlefields.get(player1.getId()).indexOf(fish);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(fishIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addFish(Player player, boolean tapped) {
        Permanent perm = new Permanent(new IslandFishJasconius());
        perm.setSummoningSick(false);
        if (tapped) {
            perm.tap();
        }
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (untap)
    }
}
