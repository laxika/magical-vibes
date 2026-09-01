package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StriderHarness;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaubahnBullOfAlaMhigo.class, GiantGrowth.class, GrizzlyBears.class, StriderHarness.class})
class RaubahnBullOfAlaMhigoTest extends BaseCardTest {

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay life")
    void wardCountersUnpaidSpell() {
        Permanent raubahn = addReadyRaubahn(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, raubahn.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Giant Growth");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Ward uses Raubahn's power when it resolves")
    void wardUsesCurrentPower() {
        Permanent raubahn = addReadyRaubahn(player1);
        raubahn.setPowerModifier(1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, raubahn.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gqs.getEffectivePower(gd, raubahn)).isEqualTo(6);
    }

    @Test
    @DisplayName("Attacking attaches an optional Equipment to a target attacking creature")
    void attackAttachesEquipmentToTargetAttacker() {
        Permanent raubahn = addReadyRaubahn(player1);
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new StriderHarness());
        equipment.setAttachedTo(raubahn.getId());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Attacking may decline the Equipment target")
    void attackMayDeclineEquipment() {
        addReadyRaubahn(player1);
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new StriderHarness());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
    }

    private Permanent addReadyRaubahn(Player player) {
        return addReadyCreature(player, new RaubahnBullOfAlaMhigo());
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
