package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OwlinShieldmageTest extends BaseCardTest {

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay 3 life")
    void wardCountersUnpaidSpell() {
        Permanent owlin = addReadyOwlin(player1);
        castShockAt(player2, owlin);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Paying 3 life lets an opponent's spell resolve")
    void payingWardLifeLetsSpellResolve() {
        Permanent owlin = addReadyOwlin(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, owlin.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gqs.getEffectivePower(gd, owlin)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, owlin)).isEqualTo(6);
    }

    @Test
    @DisplayName("Ward also counters an opponent's activated ability when they do not pay 3 life")
    void wardCountersUnpaidActivatedAbility() {
        Permanent owlin = addReadyOwlin(player1);
        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        icyManipulator.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(icyManipulator), null, owlin.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(owlin.isTapped()).isFalse();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    private void castShockAt(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
    }

    private Permanent addReadyOwlin(Player player) {
        Permanent owlin = new Permanent(new OwlinShieldmage());
        owlin.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(owlin);
        return owlin;
    }
}
