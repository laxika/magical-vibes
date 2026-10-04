package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlameWreathedPhoenix.class})
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

    @Test
    @DisplayName("Dying before the enters trigger resolves does not return the Phoenix to hand")
    void diesBeforeGainingReturnAbility() {
        Permanent phoenix = castPhoenix();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gqs.hasKeyword(gd, phoenix, Keyword.HASTE)).isFalse();
        phoenix.setMarkedDamage(gqs.getEffectiveToughness(gd, phoenix));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Flame-Wreathed Phoenix");
        harness.assertNotInHand(player1, "Flame-Wreathed Phoenix");
    }

    @Test
    @DisplayName("The granted haste and death ability last beyond the turn of entry")
    void grantedAbilitiesPersistAcrossTurns() {
        Permanent phoenix = castPhoenix();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, phoenix, Keyword.HASTE)).isTrue();
        phoenix.setMarkedDamage(gqs.getEffectiveToughness(gd, phoenix));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Flame-Wreathed Phoenix");
        harness.assertNotInGraveyard(player1, "Flame-Wreathed Phoenix");
    }

    @Test
    @DisplayName("Recasting the returned Phoenix with tribute paid does not retain its granted abilities")
    void recastDoesNotRetainGrantedAbilities() {
        Permanent phoenix = castPhoenix();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        phoenix.setMarkedDamage(gqs.getEffectiveToughness(gd, phoenix));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        var returnedCard = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getId().equals(phoenix.getCard().getId()))
                .findFirst().orElseThrow();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, returnedCard, "{2}{R}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        Permanent recastPhoenix = findPermanent(player1, "Flame-Wreathed Phoenix");
        assertThat(gqs.hasKeyword(gd, recastPhoenix, Keyword.HASTE)).isFalse();
        recastPhoenix.setMarkedDamage(gqs.getEffectiveToughness(gd, recastPhoenix));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Flame-Wreathed Phoenix");
        harness.assertNotInHand(player1, "Flame-Wreathed Phoenix");
    }

    private Permanent castPhoenix() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new FlameWreathedPhoenix(), "{2}{R}{R}");
        harness.passBothPriorities();
        return findPermanent(player1, "Flame-Wreathed Phoenix");
    }
}
