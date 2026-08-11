package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TrialOfZeal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KirolAttentiveFirstYearTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a triggered ability after tapping two creatures")
    void copiesTriggeredAbilityAfterTappingTwoCreatures() {
        harness.setLife(player2, 20);
        Permanent kirol = addCreatureReady(player1, new KirolAttentiveFirstYear());
        Permanent firstCost = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCost = addCreatureReady(player1, new GrizzlyBears());
        UUID triggerId = createTrialOfZealTrigger();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(kirol), null, triggerId);
        harness.handlePermanentChosen(player1, firstCost.getId());
        harness.handlePermanentChosen(player1, secondCost.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstCost.isTapped()).isTrue();
        assertThat(secondCost.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Can activate only once each turn")
    void canActivateOnlyOnceEachTurn() {
        Permanent kirol = addCreatureReady(player1, new KirolAttentiveFirstYear());
        Permanent firstCost = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCost = addCreatureReady(player1, new GrizzlyBears());
        UUID triggerId = createTrialOfZealTrigger();
        int kirolIndex = gd.playerBattlefields.get(player1.getId()).indexOf(kirol);

        harness.activateAbility(player1, kirolIndex, null, triggerId);
        harness.handlePermanentChosen(player1, firstCost.getId());
        harness.handlePermanentChosen(player1, secondCost.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, kirolIndex, null, triggerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires two untapped creatures to pay the activation cost")
    void requiresTwoUntappedCreatures() {
        Permanent kirol = addCreatureReady(player1, new KirolAttentiveFirstYear());
        addCreatureReady(player1, new GrizzlyBears());
        UUID triggerId = createTrialOfZealTrigger();

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(kirol),
                null,
                triggerId
        )).isInstanceOf(IllegalStateException.class);
    }

    private UUID createTrialOfZealTrigger() {
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        return gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();
    }
}
