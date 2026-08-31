package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({NadirKraken.class, GrizzlyBears.class})
class NadirKrakenTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} after drawing adds a counter and creates a Tentacle")
    void payingAfterDrawingAddsCounterAndCreatesTentacle() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new NadirKraken());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        draw();
        resolveTopOfStack();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(kraken.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, kraken)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kraken)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        Permanent tentacle = findPermanents(player1, "Tentacle").getFirst();
        assertThat(tentacle.getEffectivePower()).isEqualTo(1);
        assertThat(tentacle.getEffectiveToughness()).isEqualTo(1);
        assertThat(tentacle.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(tentacle.getCard().getSubtypes()).containsExactly(CardSubtype.TENTACLE);
    }

    @Test
    @DisplayName("Declining the payment after drawing does nothing")
    void decliningPaymentDoesNothing() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new NadirKraken());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        draw();
        resolveTopOfStack();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(kraken.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Tentacle")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private void draw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
