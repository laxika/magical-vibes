package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CoilingStalker;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BitingPalmNinja.class, Forest.class, CoilingStalker.class, LeylineOfSanctity.class})
class BitingPalmNinjaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a menace counter")
    void entersWithMenaceCounter() {
        Permanent ninja = harness.enterBattlefieldAndReturn(player1, new BitingPalmNinja());

        assertThat(ninja.getCounterCount(CounterType.MENACE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the combat-damage ability keeps the menace counter")
    void decliningCombatDamageAbilityKeepsCounter() {
        Permanent ninja = addAttackingNinja();
        harness.setHand(player2, List.of(new CoilingStalker()));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(ninja.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Removing the menace counter reveals the hand and exiles a chosen nonland card")
    void removesCounterAndExilesChosenNonlandCard() {
        Permanent ninja = addAttackingNinja();
        CoilingStalker stalker = new CoilingStalker();
        harness.setHand(player2, List.of(new Forest(), stalker));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        assertThat(ninja.getCounterCount(CounterType.MENACE)).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(stalker);
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("A hand containing only lands gives no legal card to exile")
    void onlyLandsInHandGiveNoLegalChoice() {
        Permanent ninja = addAttackingNinja();
        harness.setHand(player2, List.of(new Forest()));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(ninja.getCounterCount(CounterType.MENACE)).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Ninjutsu returns the attacker and enters tapped and attacking with a menace counter")
    void ninjutsuEntersTappedAndAttackingWithCounter() {
        Permanent attacker = addCreatureReady(player1, new BitingPalmNinja());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        BitingPalmNinja incoming = new BitingPalmNinja();
        harness.setHand(player1, List.of(incoming));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateHandAbility(player1, 0, attacker.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerHands.get(player1.getId())).contains(attacker.getCard(), incoming);

        harness.passBothPriorities();

        Permanent ninja = findPermanent(player1, "Biting-Palm Ninja");
        assertThat(ninja.isTapped()).isTrue();
        assertThat(ninja.isAttacking()).isTrue();
        assertThat(ninja.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(ninja.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(incoming);
    }

    @Test
    @DisplayName("No menace counter means no hand reveal or exile")
    void cannotExileWithoutMenaceCounter() {
        Permanent ninja = addAttackingNinja();
        ninja.setCounterCount(CounterType.MENACE, 0);
        CoilingStalker stalker = new CoilingStalker();
        harness.setHand(player2, List.of(stalker));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(stalker);
    }

    @Test
    @DisplayName("An empty hand still allows removing the menace counter")
    void emptyHandStillAllowsRemovingCounter() {
        Permanent ninja = addAttackingNinja();
        harness.setHand(player2, List.of());

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(ninja.getCounterCount(CounterType.MENACE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Removing one of multiple menace counters still exiles exactly one card")
    void removesOnlyOneCounterAndExilesOnlyOneCard() {
        Permanent ninja = addAttackingNinja();
        ninja.setCounterCount(CounterType.MENACE, 2);
        CoilingStalker first = new CoilingStalker();
        CoilingStalker second = new CoilingStalker();
        harness.setHand(player2, List.of(first, second));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(ninja.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first);
    }

    @Test
    @CardUsed({BitingPalmNinja.class, CoilingStalker.class, LeylineOfSanctity.class})
    @DisplayName("Player hexproof does not prevent the nontargeting hand exile")
    void exilesFromDamagedPlayerWithHexproof() {
        Permanent ninja = addAttackingNinja();
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        CoilingStalker stalker = new CoilingStalker();
        harness.setHand(player2, List.of(stalker));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(ninja.getCounterCount(CounterType.MENACE)).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(stalker);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private Permanent addAttackingNinja() {
        Permanent ninja = harness.enterBattlefieldAndReturn(player1, new BitingPalmNinja());
        ninja.setSummoningSick(false);
        ninja.setAttacking(true);
        return ninja;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
