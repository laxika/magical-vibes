package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormGodsOracle.class, GrizzlyBears.class, LightningBolt.class})
class StormGodsOracleTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability gives Storm God's Oracle +1/-1 until end of turn")
    void activatedAbilityBoostsSelfUntilEndOfTurn() {
        Permanent oracle = addReadyOracle(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(oracle.getEffectivePower()).isEqualTo(2);
        assertThat(oracle.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(oracle.getEffectivePower()).isEqualTo(1);
        assertThat(oracle.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("When Storm God's Oracle dies, it deals 3 damage to a player")
    void deathTriggerDealsDamageToPlayer() {
        Permanent oracle = harness.addToBattlefieldAndReturn(player1, new StormGodsOracle());
        harness.setLife(player2, 20);

        killOracle(oracle.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("When Storm God's Oracle dies, it can deal 3 damage to a creature")
    void deathTriggerDealsDamageToCreature() {
        Permanent oracle = harness.addToBattlefieldAndReturn(player1, new StormGodsOracle());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID bearsId = bears.getId();

        killOracle(oracle.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private Permanent addReadyOracle(Player player) {
        Permanent oracle = new Permanent(new StormGodsOracle());
        oracle.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(oracle);
        return oracle;
    }

    private void killOracle(UUID oracleId) {
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, oracleId);
        harness.passBothPriorities();
    }
}
