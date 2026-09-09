package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DominatingLicid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThalakosScout.class, ThalakosDrifters.class, DominatingLicid.class})
class ThalakosScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card puts Thalakos Scout's return ability on the stack")
    void discardCostPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new ThalakosScout());
        harness.setHand(player1, List.of(new ThalakosDrifters()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        harness.assertInGraveyard(player1, "Thalakos Drifters");
    }

    @Test
    @DisplayName("Discarding a card returns Thalakos Scout to its owner's hand")
    void discardCostReturnsThalakosScoutToHand() {
        harness.addToBattlefield(player1, new ThalakosScout());
        harness.setHand(player1, List.of(new ThalakosDrifters()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thalakos Drifters");
        harness.assertInHand(player1, "Thalakos Scout");
        harness.assertNotOnBattlefield(player1, "Thalakos Scout");
    }

    @Test
    @DisplayName("Thalakos Scout cannot activate without a card in hand")
    void cannotActivateWithoutCardInHand() {
        harness.addToBattlefield(player1, new ThalakosScout());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returning a controlled Thalakos Scout puts it into its owner's hand")
    void returnsControlledScoutToOwnersHand() {
        Permanent scout = harness.addToBattlefieldAndReturn(player2, new ThalakosScout());
        addCreatureReady(player1, new DominatingLicid());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, scout.getId());
        harness.passBothPriorities();

        assertThat(gd.findControllerOf(scout)).isEqualTo(player1.getId());
        harness.setHand(player1, List.of(new ThalakosDrifters()));

        int scoutIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scout);
        harness.activateAbility(player1, scoutIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thalakos Drifters");
        harness.assertInHand(player2, "Thalakos Scout");
        harness.assertNotInHand(player1, "Thalakos Scout");
        harness.assertNotOnBattlefield(player1, "Thalakos Scout");
        harness.assertNotOnBattlefield(player2, "Thalakos Scout");
    }

    @Test
    @DisplayName("Shadow prevents a non-shadow creature from blocking Thalakos Scout")
    void shadowPreventsNonShadowCreatureFromBlocking() {
        Permanent scout = addCreatureReady(player1, new ThalakosScout());
        scout.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ThalakosDrifters());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scout);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("Shadow prevents Thalakos Scout from blocking a non-shadow creature")
    void shadowPreventsScoutFromBlockingNonShadowCreature() {
        Permanent attacker = addCreatureReady(player1, new ThalakosDrifters());
        attacker.setAttacking(true);
        Permanent scout = addCreatureReady(player2, new ThalakosScout());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(scout);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }
}
