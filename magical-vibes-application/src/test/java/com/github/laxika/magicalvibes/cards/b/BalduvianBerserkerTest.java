package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalduvianBerserker.class, GrizzlyBears.class, Murder.class})
class BalduvianBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Enlist taps a nonattacking creature and boosts the attacker by its power")
    void enlistBoostsAttackerBySupporterPower() {
        Permanent berserker = addCreatureReady(player1, new BalduvianBerserker());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(supporter.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();

        assertThat(supporter.isTapped()).isTrue();
        assertThat(berserker.getPowerModifier()).isEqualTo(2);
        assertThat(berserker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("When it dies, it deals damage equal to its power to any target")
    void deathTriggerDealsDamageEqualToPower() {
        Permanent berserker = addCreatureReady(player1, new BalduvianBerserker());
        berserker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, berserker.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Balduvian Berserker");
    }
}
