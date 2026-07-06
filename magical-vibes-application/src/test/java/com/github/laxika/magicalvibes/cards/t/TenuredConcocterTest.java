package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TenuredConcocterTest extends BaseCardTest {

    

    @Test
    @DisplayName("No infusion bonus when you have not gained life this turn")
    void noBonusWithoutLifeGain() {
        harness.addToBattlefield(player1, new TenuredConcocter());

        Permanent concocter = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, concocter)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +2/+0 while you have gained life this turn")
    void bonusWhileLifeGained() {
        harness.addToBattlefield(player1, new TenuredConcocter());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        Permanent concocter = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, concocter)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, concocter)).isEqualTo(5);
    }

    @Test
    @DisplayName("Triggers and draws when an opponent's spell targets this creature")
    void triggersOnOpponentSpell() {
        harness.addToBattlefield(player1, new TenuredConcocter());
        UUID concocterId = harness.getPermanentId(player1, "Tenured Concocter");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, concocterId);

        // Shock + Tenured Concocter's triggered ability on the stack.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Tenured Concocter");

        harness.passBothPriorities(); // resolve trigger → may prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does NOT trigger when the controller's own spell targets it")
    void doesNotTriggerOnOwnSpell() {
        harness.addToBattlefield(player1, new TenuredConcocter());
        UUID concocterId = harness.getPermanentId(player1, "Tenured Concocter");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, concocterId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("Triggers when an opponent's activated ability targets this creature")
    void triggersOnOpponentAbility() {
        harness.addToBattlefield(player1, new TenuredConcocter());
        UUID concocterId = harness.getPermanentId(player1, "Tenured Concocter");

        Permanent firecannon = new Permanent(new ElaborateFirecannon());
        firecannon.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(firecannon);

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, concocterId);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Tenured Concocter");
    }
}
