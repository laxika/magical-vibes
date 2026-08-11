package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GracefulAntelopeTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may make a target land a Plains until the Antelope leaves")
    void combatDamageChangesLandUntilSourceLeaves() {
        Permanent antelope = attackWithAntelope(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombatAndTrigger();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.PLAINS);

        gd.playerBattlefields.get(player1.getId()).remove(antelope);

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("Declining the combat trigger leaves the target land unchanged")
    void decliningCombatTriggerDoesNothing() {
        attackWithAntelope(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("The combat trigger cannot choose a non-land permanent")
    void cannotChooseNonLandPermanent() {
        attackWithAntelope(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();
    }

    private Permanent attackWithAntelope(Player player) {
        Permanent antelope = addCreatureReady(player, new GracefulAntelope());
        antelope.setAttacking(true);
        antelope.setAttackTarget(player2.getId());
        return antelope;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat(player1);
        harness.passBothPriorities();
    }
}
