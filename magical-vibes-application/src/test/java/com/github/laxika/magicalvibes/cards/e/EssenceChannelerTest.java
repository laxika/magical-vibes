package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({EssenceChanneler.class, AngelOfMercy.class, DoomBlade.class, GrizzlyBears.class})
class EssenceChannelerTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying and vigilance only after its controller loses life")
    void gainsEvasionAfterControllerLosesLife() {
        Permanent channeler = addCreatureReady(player1, new EssenceChanneler());

        assertThat(gqs.hasKeyword(gd, channeler, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, channeler, Keyword.VIGILANCE)).isFalse();

        gd.lifeLostThisTurn.put(player1.getId(), 1);

        assertThat(gqs.hasKeyword(gd, channeler, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, channeler, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Gains a +1/+1 counter when its controller gains life")
    void gainsCounterOnLifeGain() {
        Permanent channeler = addCreatureReady(player1, new EssenceChanneler());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(channeler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Moves all counters to a target creature you control when it dies")
    void movesCountersToControlledCreatureOnDeath() {
        Permanent channeler = addCreatureReady(player1, new EssenceChanneler());
        channeler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        channeler.setCounterCount(CounterType.CHARGE, 1);
        channeler.tap();
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, channeler.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ally.getId())
                .doesNotContain(opposingCreature.getId());

        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ally.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }
}
