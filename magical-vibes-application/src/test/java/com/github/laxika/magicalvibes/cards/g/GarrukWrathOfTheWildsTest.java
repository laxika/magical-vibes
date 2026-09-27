package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarrukWrathOfTheWilds.class, GrizzlyBears.class})
class GarrukWrathOfTheWildsTest extends BaseCardTest {

    @Test
    void plusOneAppliesBothPerpetualChangesToTheChosenCreatureCard() {
        Permanent garruk = addReadyGarruk(4);
        GrizzlyBears bear = new GrizzlyBears();
        harness.setHand(player1, List.of(bear));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualPowerToughnessChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent enteredBear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bear.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, enteredBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enteredBear)).isEqualTo(3);
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void minusOneDraftsAChoiceOntoTheBattlefield() {
        Permanent garruk = addReadyGarruk(2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getId().equals(drafted.getId()));
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void minusSixBoostsOwnCreaturesAndGrantsTrampleUntilEndOfTurn() {
        Permanent garruk = addReadyGarruk(6);
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyGarruk(int loyalty) {
        Permanent permanent = new Permanent(new GarrukWrathOfTheWilds());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
