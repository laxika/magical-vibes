package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaggedLightning;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeylineOfCombustionTest extends BaseCardTest {

    @Test
    @DisplayName("Leyline in opening hand may begin the game on the battlefield")
    void leylineInOpeningHandMayStartOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfCombustion()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);

        assertThat(openingHarness.getGameData().playerBattlefields
                .get(openingHarness.getPlayer1().getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline of Combustion"));
    }

    @Test
    @DisplayName("Opponent targeting your permanent causes them to take 2 damage")
    void opponentTargetingPermanentDealsDamageToOpponent() {
        harness.addToBattlefield(player1, new LeylineOfCombustion());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        forceOpponentMainPhase();

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Opponent targeting your player causes them to take 2 damage")
    void opponentTargetingPlayerDealsDamageToOpponent() {
        harness.addToBattlefield(player1, new LeylineOfCombustion());
        harness.setHand(player2, List.of(new LavaAxe()));
        harness.addMana(player2, ManaColor.RED, 5);
        forceOpponentMainPhase();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("An opponent ability targeting your permanent causes its controller to take 2 damage")
    void opponentAbilityTargetingPermanentDealsDamageToOpponent() {
        harness.addToBattlefield(player1, new LeylineOfCombustion());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new ZuranSpellcaster());
        forceOpponentMainPhase();

        harness.activateAbility(player2, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Multiple targets on one opposing spell cause only one Leyline trigger")
    void multipleTargetsCauseOneTrigger() {
        harness.addToBattlefield(player1, new LeylineOfCombustion());
        Permanent firstTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new JaggedLightning()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        forceOpponentMainPhase();

        harness.castSorcery(player2, 0, List.of(firstTarget.getId(), secondTarget.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void forceOpponentMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
