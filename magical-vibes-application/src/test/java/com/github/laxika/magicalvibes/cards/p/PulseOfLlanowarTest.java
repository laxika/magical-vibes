package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KeldonNecropolis;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PulseOfLlanowar.class, Forest.class, KeldonNecropolis.class})
class PulseOfLlanowarTest extends BaseCardTest {

    @Test
    void basicLandYouControlProducesChosenColor() {
        harness.addToBattlefield(player1, new PulseOfLlanowar());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void doesNotAffectBasicLandControlledByOpponent() {
        harness.addToBattlefield(player1, new PulseOfLlanowar());
        harness.addToBattlefield(player2, new Forest());

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.tapPermanent(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void doesNotAffectNonbasicLandYouControl() {
        harness.addToBattlefield(player1, new PulseOfLlanowar());
        harness.addToBattlefield(player1, new KeldonNecropolis());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
