package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoldgrafMillipedeTest extends BaseCardTest {

    private Permanent castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MoldgrafMillipede()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Moldgraf Millipede"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("ETB mills three and puts counters for milled creatures")
    void etbMillsAndCountersFromMilledCreatures() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Forest()));

        Permanent millipede = castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(millipede.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB counters count existing graveyard creatures plus milled")
    void etbCountersIncludeExistingGraveyardCreatures() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new Forest(), new Forest(), new Forest()));

        Permanent millipede = castAndResolveEtb();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(millipede.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB puts no counters when graveyard has no creatures after mill")
    void etbNoCountersWhenNoCreaturesInGraveyard() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new Forest(), new Forest(), new Forest()));

        Permanent millipede = castAndResolveEtb();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(millipede.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
