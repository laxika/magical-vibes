package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
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

@CardUsed({TermagantSwarm.class, GoForTheThroat.class})
class TermagantSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Ravenous does not draw below X=5")
    void ravenousDoesNotDrawBelowThreshold() {
        harness.setLibrary(player1, List.of(new TermagantSwarm()));
        castTermagantSwarm(4);

        assertThat(findPermanent(player1, "Termagant Swarm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ravenous enters with X +1/+1 counters and draws at X=5")
    void ravenousEntersWithCountersAndDrawsAtThreshold() {
        harness.setLibrary(player1, List.of(new TermagantSwarm()));
        castTermagantSwarm(5);

        assertThat(findPermanent(player1, "Termagant Swarm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        harness.assertInHand(player1, "Termagant Swarm");
    }

    @Test
    @DisplayName("Death Frenzy creates one Tyranid per point of the dying swarm's power")
    void deathFrenzyCreatesTokensEqualToPower() {
        Permanent swarm = castTermagantSwarm(3);

        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, swarm.getId());
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Tyranid");
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.TYRANID);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("X=0 dies without drawing or creating tokens")
    void zeroXDiesWithoutDrawingOrCreatingTokens() {
        harness.setLibrary(player1, List.of(new TermagantSwarm()));
        harness.setHand(player1, List.of(new TermagantSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Termagant Swarm");
        harness.assertInGraveyard(player1, "Termagant Swarm");
        assertThat(findPermanents(player1, "Tyranid")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Death Frenzy uses power at death rather than the original X")
    void deathFrenzyUsesChangedPower() {
        Permanent swarm = castTermagantSwarm(3);
        swarm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, swarm.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Termagant Swarm");
        assertThat(findPermanents(player1, "Tyranid")).hasSize(6);
        assertThat(findPermanents(player2, "Tyranid")).isEmpty();
    }

    @Test
    @DisplayName("Ravenous still draws if the swarm dies before the draw resolves")
    void ravenousDrawsAfterSourceDies() {
        harness.setLibrary(player1, List.of(new TermagantSwarm(), new TermagantSwarm()));
        harness.setHand(player1, List.of(new TermagantSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0, 6);
        harness.passBothPriorities();
        Permanent swarm = findPermanent(player1, "Termagant Swarm");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, swarm.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Termagant Swarm");
        assertThat(findPermanents(player1, "Tyranid")).hasSize(6);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Termagant Swarm");
    }

    private Permanent castTermagantSwarm(int x) {
        harness.setHand(player1, List.of(new TermagantSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x);

        harness.castCreature(player1, 0, x);
        resolveAllTriggers();
        return findPermanent(player1, "Termagant Swarm");
    }
}
