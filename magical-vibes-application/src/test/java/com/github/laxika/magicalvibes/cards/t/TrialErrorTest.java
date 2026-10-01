package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrialError.class, AzoriusFirstWing.class, MistralCharger.class})
class TrialErrorTest extends BaseCardTest {

    @Test
    @DisplayName("Trial returns creatures blocking or blocked by the target creature")
    void trialReturnsCombatOpponentsOfAttackingTarget() {
        Permanent attacker = addCreatureReady(player1, new MistralCharger());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MistralCharger());
        connectBlockerToAttacker(blocker, attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TrialError()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mistral Charger");
        harness.assertNotOnBattlefield(player2, "Mistral Charger");
        harness.assertInHand(player2, "Mistral Charger");
    }

    @Test
    @DisplayName("Trial returns every creature blocking the target creature")
    void trialReturnsAllCreaturesBlockingTarget() {
        Permanent attacker = addCreatureReady(player1, new MistralCharger());
        attacker.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new MistralCharger());
        Permanent secondBlocker = addCreatureReady(player2, new MistralCharger());
        connectBlockerToAttacker(firstBlocker, attacker);
        connectBlockerToAttacker(secondBlocker, attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TrialError()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .doesNotContain(firstBlocker.getId(), secondBlocker.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .contains(firstBlocker.getCard().getId(), secondBlocker.getCard().getId());
    }

    @Test
    @DisplayName("Trial returns attackers blocked by the target blocker but not the target")
    void trialReturnsAttackerOfBlockingTarget() {
        Permanent attacker = addCreatureReady(player2, new MistralCharger());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player1, new MistralCharger());
        connectBlockerToAttacker(blocker, attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TrialError()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, 0, blocker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mistral Charger");
        harness.assertNotOnBattlefield(player2, "Mistral Charger");
        harness.assertInHand(player2, "Mistral Charger");
    }

    @Test
    @DisplayName("Error counters a multicolored spell")
    void errorCountersMulticoloredSpell() {
        AzoriusFirstWing spell = new AzoriusFirstWing();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.setHand(player2, List.of(new TrialError()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Azorius First-Wing");
        harness.assertNotOnBattlefield(player1, "Azorius First-Wing");
    }

    @Test
    @DisplayName("Error cannot target a monocolored spell")
    void errorCannotTargetMonocoloredSpell() {
        Card spell = new MistralCharger();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new TrialError()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, spell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("multicolored");
    }

    private void connectBlockerToAttacker(Permanent blocker, Permanent attacker) {
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
    }
}
