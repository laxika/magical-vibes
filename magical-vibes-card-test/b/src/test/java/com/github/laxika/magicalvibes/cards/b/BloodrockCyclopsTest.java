package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindbornMuse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({BloodrockCyclops.class, GrizzlyBears.class, WindbornMuse.class, ChandraNalaar.class})
class BloodrockCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Bloodrock Cyclops puts it on the battlefield")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new BloodrockCyclops()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Bloodrock Cyclops");
    }

    @Test
    @DisplayName("Bloodrock Cyclops enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new BloodrockCyclops()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Bloodrock Cyclops");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Declaring Bloodrock Cyclops as attacker succeeds")
    void canDeclareAsAttacker() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new BloodrockCyclops());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Declaring no attackers when Bloodrock Cyclops can attack throws exception")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new BloodrockCyclops());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Omitting Bloodrock Cyclops from attackers while declaring other creatures throws exception")
    void mustBeIncludedAmongAttackers() {
        addCreatureReady(player1, new BloodrockCyclops());
        addCreatureReady(player1, new GrizzlyBears());

        // Declare only Grizzly Bears (index 1), omitting Bloodrock Cyclops (index 0)
        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Declaring both Bloodrock Cyclops and another creature as attackers succeeds")
    void canDeclareWithOtherAttackers() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new BloodrockCyclops());
        addCreatureReady(player1, new GrizzlyBears());

        // No exception means declaration is valid; 3 + 2 = 5 damage
        declareAttackers(List.of(0, 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Bloodrock Cyclops does not need to attack if tapped")
    void doesNotAttackIfTapped() {
        harness.setLife(player2, 20);

        Permanent cyclops = addCreatureReady(player1, new BloodrockCyclops());
        cyclops.tap();
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(cyclops.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Bloodrock Cyclops does not need to attack with summoning sickness")
    void doesNotAttackWithSummoningSickness() {
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new BloodrockCyclops());
        addCreatureReady(player1, new GrizzlyBears());

        // Only Grizzly Bears can attack (index 1), Cyclops has summoning sickness
        // so declaring just bears should succeed — only 2 damage from bears
        declareAttackers(List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Bloodrock Cyclops is not forced to attack when opponent controls Windborn Muse (attack tax)")
    void notForcedToAttackWithAttackTax() {
        Permanent cyclops = addCreatureReady(player1, new BloodrockCyclops());

        // Opponent has Windborn Muse (tax 2 per attacker)
        harness.addToBattlefield(player2, new WindbornMuse());

        // The controller may decline to pay the attack tax even with enough mana.
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(List.of());

        assertThat(cyclops.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A Windborn Muse controlled by the attacker does not exempt Cyclops from attacking")
    void ownWindbornMuseDoesNotExemptAttack() {
        addCreatureReady(player1, new BloodrockCyclops());
        harness.addToBattlefield(player1, new WindbornMuse());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("No attack is required when the only Cyclops is tapped")
    void tappedCyclopsAllowsEmptyDeclaration() {
        Permanent cyclops = addCreatureReady(player1, new BloodrockCyclops());
        cyclops.tap();
        harness.setLife(player2, 20);

        declareAttackers(List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("No attack is required when the only Cyclops has summoning sickness")
    void summoningSickCyclopsAllowsEmptyDeclaration() {
        harness.addToBattlefield(player1, new BloodrockCyclops());
        harness.setLife(player2, 20);

        declareAttackers(List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Must attack an untaxed planeswalker despite Windborn Muse")
    void mustAttackUntaxedPlaneswalkerDespiteWindbornMuse() {
        addCreatureReady(player1, new BloodrockCyclops());

        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        harness.addToBattlefield(player2, new WindbornMuse());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        gs.declareAttackers(gd, player1, List.of(0), java.util.Map.of(0, planeswalker.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getAttackTarget())
                .isEqualTo(planeswalker.getId());
    }

    @Test
    @DisplayName("Bloodrock Cyclops deals 3 combat damage when unblocked")
    void dealsThreeDamageUnblocked() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new BloodrockCyclops());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Bloodrock Cyclops trades with a 3/3 creature in combat")
    void tradesWithThreeThree() {
        Permanent cyclops = addCreatureReady(player1, new BloodrockCyclops());
        cyclops.setAttacking(true);

        addCreatureReady(player2, new BloodrockCyclops());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // through combat damage

        // Both 3/3 creatures should die
        harness.assertInGraveyard(player1, "Bloodrock Cyclops");
        harness.assertInGraveyard(player2, "Bloodrock Cyclops");
    }

    @Test
    @DisplayName("Multiple Bloodrock Cyclops must all attack")
    void multipleMusttAllAttack() {
        addCreatureReady(player1, new BloodrockCyclops());
        addCreatureReady(player1, new BloodrockCyclops());

        // Only declaring one of the two should fail
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Multiple Bloodrock Cyclops can all attack successfully")
    void multipleCanAllAttack() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new BloodrockCyclops());
        addCreatureReady(player1, new BloodrockCyclops());

        // No exception means declaration is valid; 3 + 3 = 6 damage
        declareAttackers(List.of(0, 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }
}

