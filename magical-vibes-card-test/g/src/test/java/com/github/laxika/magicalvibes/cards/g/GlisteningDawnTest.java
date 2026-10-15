package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlisteningDawn.class, Island.class})
class GlisteningDawnTest extends BaseCardTest {

    @Test
    void incubatesTwiceForNumberOfLandsYouControl() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new GlisteningDawn()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> incubators = findPermanents(player1, "Incubator");
        assertThat(incubators).hasSize(2);
        assertThat(incubators)
                .allSatisfy(incubator -> assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(3));
    }

    @Test
    void doesNotCountOpponentsLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new GlisteningDawn()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Incubator")).hasSize(2)
                .allSatisfy(token -> assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(1));
        assertThat(findPermanents(player2, "Incubator")).isEmpty();
    }

    @Test
    void countsLandsAtResolution() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new GlisteningDawn()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0);
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Incubator")).hasSize(2)
                .allSatisfy(token -> assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(2));
    }

    @Test
    void createsIncubatorArtifactsWithIncubatorSubtype() {
        harness.setHand(player1, List.of(new GlisteningDawn()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Incubator")).hasSize(2).allSatisfy(token -> {
            assertThat(gqs.isArtifact(gd, token)).isTrue();
            assertThat(gqs.isCreature(gd, token)).isFalse();
            assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.INCUBATOR)).isTrue();
        });
    }

    @Test
    void transformsIntoNamedPhyrexianArtifactCreatureAndKeepsCounters() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new GlisteningDawn()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent token = findPermanent(player1, "Incubator");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(token), null, null);
        harness.passBothPriorities();

        assertThat(token.isTransformed()).isTrue();
        assertThat(gqs.isArtifact(gd, token)).isTrue();
        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.PHYREXIAN)).isTrue();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(token.getCard().getName()).isEqualTo("Phyrexian Token");
        assertThat(findPermanents(player1, "Incubator")).hasSize(1);
    }

    @Test
    void zeroLandsStillCreatesTwoTokensAndTransformedZeroZeroDies() {
        harness.setHand(player1, List.of(new GlisteningDawn()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        List<Permanent> tokens = findPermanents(player1, "Incubator");
        assertThat(tokens).hasSize(2).allSatisfy(token ->
                assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
        Permanent token = tokens.getFirst();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(token), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token).contains(tokens.get(1));
    }
}
