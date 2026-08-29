package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlameWreathedPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Paying tribute puts two +1/+1 counters on Flame-Wreathed Phoenix without granting haste")
    void tributePaid() {
        Permanent phoenix = castPhoenix();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(phoenix.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, phoenix, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Declining tribute grants haste and returns the Phoenix to its owner's hand when it dies")
    void tributeNotPaidReturnsToHand() {
        Permanent phoenix = castPhoenix();
        var phoenixId = phoenix.getCard().getId();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, phoenix, Keyword.HASTE)).isTrue();

        phoenix.setMarkedDamage(gqs.getEffectiveToughness(gd, phoenix));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenixId));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(phoenixId));
    }

    @Test
    @DisplayName("Paying tribute does not return the Phoenix from its owner's graveyard when it dies")
    void tributePaidDoesNotReturnToHand() {
        Permanent phoenix = castPhoenix();
        var phoenixId = phoenix.getCard().getId();
        harness.handleMayAbilityChosen(player2, true);

        phoenix.setMarkedDamage(gqs.getEffectiveToughness(gd, phoenix));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenixId));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(phoenixId));
    }

    private Permanent castPhoenix() {
        harness.setHand(player1, java.util.List.of(new FlameWreathedPhoenix()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Flame-Wreathed Phoenix");
    }
}
