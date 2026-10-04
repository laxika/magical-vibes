package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EntrapmentManeuver.class, AzureDrake.class, GrizzlyBears.class})
class EntrapmentManeuverTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a target player's attacking creature and creates Soldier tokens for the caster")
    void sacrificesAttackingCreatureAndCreatesTokensForCaster() {
        Permanent attacker = addAttackingCreature(player2, new AzureDrake());
        Permanent nonAttacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAtPlayer2();

        harness.assertNotOnBattlefield(player2, "Azure Drake");
        harness.assertInGraveyard(player2, "Azure Drake");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(4);
        assertThat(countPermanents(player2, "Soldier")).isZero();
        assertThat(nonAttacker).isIn(gd.playerBattlefields.get(player2.getId()));
        assertThat(attacker).isNotIn(gd.playerBattlefields.get(player2.getId()));
    }

    @Test
    @DisplayName("The target player chooses among multiple attacking creatures")
    void targetPlayerChoosesAttackingCreature() {
        Permanent drake = addAttackingCreature(player2, new AzureDrake());
        Permanent bears = addAttackingCreature(player2, new GrizzlyBears());

        castAtPlayer2();
        harness.handlePermanentChosen(player2, bears.getId());

        harness.assertOnBattlefield(player2, "Azure Drake");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
        assertThat(drake).isIn(gd.playerBattlefields.get(player2.getId()));
    }

    @Test
    @DisplayName("Does nothing when the target player controls no attacking creature")
    void noAttackingCreatureDoesNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAtPlayer2();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Soldier")).isZero();
    }

    @Test
    @DisplayName("Can target the caster and sacrifice their own attacking creature")
    void canTargetCaster() {
        Permanent attacker = addAttackingCreature(player1, new GrizzlyBears());
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new EntrapmentManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
        assertThat(countPermanents(player2, "Soldier")).isZero();
    }

    @Test
    @DisplayName("Uses toughness including counters before the creature leaves the battlefield")
    void usesModifiedToughnessBeforeSacrifice() {
        Permanent attacker = addAttackingCreature(player2, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        castAtPlayer2();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(5);
    }

    @Test
    @DisplayName("Marked damage does not reduce the number of Soldier tokens")
    void markedDamageDoesNotReduceTokenCount() {
        Permanent attacker = addAttackingCreature(player2, new AzureDrake());
        attacker.setMarkedDamage(3);

        castAtPlayer2();

        harness.assertInGraveyard(player2, "Azure Drake");
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(4);
    }

    private void castAtPlayer2() {
        harness.setHand(player1, List.of(new EntrapmentManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }

    private Permanent addAttackingCreature(com.github.laxika.magicalvibes.model.Player player,
                                           com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(true);
        permanent.setAttackTarget(player1.getId());
        return permanent;
    }
}
