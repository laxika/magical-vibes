package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlibansTower;
import com.github.laxika.magicalvibes.cards.d.DrySpell;
import com.github.laxika.magicalvibes.cards.s.SengirAutocrat;
import com.github.laxika.magicalvibes.cards.e.EronTheRelentless;
import com.github.laxika.magicalvibes.cards.f.FeastOfTheUnicorn;
import com.github.laxika.magicalvibes.cards.g.GrandmotherSengir;
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

@CardUsed({CemeteryGate.class, SengirAutocrat.class, EronTheRelentless.class, FeastOfTheUnicorn.class,
        AlibansTower.class, GrandmotherSengir.class, DrySpell.class})
class CemeteryGateTest extends BaseCardTest {

    @Test
    @DisplayName("Takes no combat damage from a black attacker")
    void takesNoDamageFromBlack() {
        addCreatureReady(player1, new SengirAutocrat());
        Permanent blocker = addCreatureReady(player2, new CemeteryGate());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Cemetery Gate");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Defender prevents Cemetery Gate from attacking")
    void cannotAttackBecauseOfDefender() {
        addCreatureReady(player1, new CemeteryGate());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Protection prevents untargeted black spell damage")
    void preventsUntargetedBlackDamage() {
        Permanent gate = addCreatureReady(player2, new CemeteryGate());
        Permanent unprotected = addCreatureReady(player2, new SengirAutocrat());

        harness.castFromHand(player1, new DrySpell(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gate.getMarkedDamage()).isZero();
        assertThat(unprotected.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Cemetery Gate");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Protection also prevents targeting by its controller's black ability")
    void cannotBeTargetedByOwnBlackAbility() {
        addCreatureReady(player1, new GrandmotherSengir());
        Permanent gate = addCreatureReady(player1, new CemeteryGate());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gate.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Takes lethal combat damage from a red attacker")
    void takesDamageFromRed() {
        Permanent attacker = addCreatureReady(player1, new EronTheRelentless());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new CemeteryGate());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Cemetery Gate");
        harness.assertInGraveyard(player2, "Cemetery Gate");
    }

    @Test
    @DisplayName("Cannot be targeted by a black ability")
    void cannotBeTargetedByBlackAbility() {
        addCreatureReady(player1, new GrandmotherSengir());
        Permanent gate = addCreatureReady(player2, new CemeteryGate());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gate.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be targeted by a red instant")
    void canBeTargetedByRedInstant() {
        Permanent gate = addCreatureReady(player1, new CemeteryGate());
        gate.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AlibansTower()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, gate.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Aliban's Tower");

        harness.passBothPriorities();

        assertThat(gate.getPowerModifier()).isEqualTo(3);
        assertThat(gate.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot be enchanted by a black Aura")
    void cannotBeEnchantedByBlackAura() {
        Permanent gate = addCreatureReady(player2, new CemeteryGate());
        harness.setHand(player1, List.of(new FeastOfTheUnicorn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, gate.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }
}
