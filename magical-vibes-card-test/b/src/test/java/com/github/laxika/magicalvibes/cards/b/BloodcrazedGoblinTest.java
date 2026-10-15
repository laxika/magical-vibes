package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodcrazedGoblin.class, LightningBolt.class})
class BloodcrazedGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack when no opponent has been dealt damage this turn")
    void cannotAttackWhenNoOpponentDealtDamage() {
        addCreatureReady(player1, new BloodcrazedGoblin());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when an opponent has been dealt damage this turn")
    void canAttackWhenOpponentDealtDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BloodcrazedGoblin());
        gd.recordDamageToPlayer(player2.getId(), 1);

        declareAttackers(player1, List.of(0));

        // Attack went through — opponent takes combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot attack when only the controller has been dealt damage")
    void cannotAttackWhenOnlyControllerDealtDamage() {
        addCreatureReady(player1, new BloodcrazedGoblin());
        gd.recordDamageToPlayer(player1.getId(), 1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restriction is cleared at the start of a new turn")
    void restrictionClearedOnNewTurn() {
        addCreatureReady(player1, new BloodcrazedGoblin());
        gd.recordDamageToPlayer(player2.getId(), 1);

        // Simulate new turn clearing the trackers (as TurnProgressionService does)
        gd.playersDealtDamageThisTurn.clear();
        gd.damageDealtToPlayersThisTurn.clear();

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncombat spell damage allows attacking even after the opponent gains life")
    void spellDamageAllowsAttackAfterLifeGain() {
        addCreatureReady(player1, new BloodcrazedGoblin());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 17);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Damage to an opponent's creature does not allow attacking")
    void damageToOpponentsCreatureDoesNotAllowAttack() {
        addCreatureReady(player1, new BloodcrazedGoblin());
        var victim = addCreatureReady(player2, new BloodcrazedGoblin());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, victim.getId());
        harness.assertInGraveyard(player2, "Bloodcrazed Goblin");

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A lower opponent life total without damage does not allow attacking")
    void reducedLifeTotalDoesNotAllowAttack() {
        addCreatureReady(player1, new BloodcrazedGoblin());
        harness.setLife(player2, 19);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage during the previous turn does not allow attacking on the next turn")
    void actualTurnTransitionClearsDamageEligibility() {
        addCreatureReady(player2, new BloodcrazedGoblin());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.assertLife(player1, 17);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
