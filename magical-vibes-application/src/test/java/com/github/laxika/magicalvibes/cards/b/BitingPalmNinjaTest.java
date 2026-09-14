package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BitingPalmNinja.class, Forest.class, GrizzlyBears.class})
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
        harness.setHand(player2, List.of(new GrizzlyBears()));

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
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(new Forest(), bears));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        assertThat(ninja.getCounterCount(CounterType.MENACE)).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears);
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
