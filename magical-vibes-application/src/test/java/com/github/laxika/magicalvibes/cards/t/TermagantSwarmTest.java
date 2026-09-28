package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TermagantSwarm.class, GrizzlyBears.class, Murder.class})
class TermagantSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Ravenous does not draw below X=5")
    void ravenousDoesNotDrawBelowThreshold() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castTermagantSwarm(4);

        assertThat(findPermanent(player1, "Termagant Swarm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ravenous enters with X +1/+1 counters and draws at X=5")
    void ravenousEntersWithCountersAndDrawsAtThreshold() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castTermagantSwarm(5);

        assertThat(findPermanent(player1, "Termagant Swarm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Death Frenzy creates one Tyranid per point of the dying swarm's power")
    void deathFrenzyCreatesTokensEqualToPower() {
        Permanent swarm = castTermagantSwarm(3);

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castSorcery(player2, 0, swarm.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Tyranid");
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.TYRANID);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    private Permanent castTermagantSwarm(int x) {
        harness.setHand(player1, List.of(new TermagantSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x);

        harness.castCreature(player1, 0, x);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Termagant Swarm");
    }
}
