package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvenArcher;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GracefulAntelope.class, Mountain.class, AvenArcher.class})
class GracefulAntelopeTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may make a target land a Plains until the Antelope leaves")
    void combatDamageChangesLandUntilSourceLeaves() {
        Permanent antelope = attackWithAntelope(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombatAndTrigger();
        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

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
        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("The combat trigger cannot choose a non-land permanent")
    void cannotChooseNonLandPermanent() {
        attackWithAntelope(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent creature = addCreatureReady(player2, new AvenArcher());

        resolveCombatAndTrigger();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("Combat damage dealt to a creature does not trigger the ability")
    void combatDamageToCreatureDoesNotTrigger() {
        Permanent antelope = attackWithAntelope(player1);
        Permanent blocker = addCreatureReady(player2, new AvenArcher());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(antelope))));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The accepted effect does nothing if the Antelope leaves before resolution")
    void sourceLeavingBeforeResolutionPreventsLandChange() {
        Permanent antelope = attackWithAntelope(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombatAndTrigger();
        harness.handlePermanentChosen(player1, mountain.getId());
        gd.playerBattlefields.get(player1.getId()).remove(antelope);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.MOUNTAIN);
    }

    private Permanent attackWithAntelope(Player player) {
        Permanent antelope = addCreatureReady(player, new GracefulAntelope());
        antelope.setAttacking(true);
        antelope.setAttackTarget(player2.getId());
        return antelope;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
