package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SiegfriedFamedSwordsman.class, Forest.class})
class SiegfriedFamedSwordsmanTest extends BaseCardTest {

    private Permanent castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SiegfriedFamedSwordsman(), "{3}{B}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Siegfried, Famed Swordsman");
    }

    @Test
    @DisplayName("ETB mills three cards and puts twice the creature count in counters")
    void etbMillsAndDoublesCreatureCount() {
        harness.setGraveyard(player1, List.of(new SiegfriedFamedSwordsman()));
        harness.setLibrary(player1, List.of(new SiegfriedFamedSwordsman(), new Forest(), new SiegfriedFamedSwordsman()));

        Permanent siegfried = castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(siegfried.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("ETB ignores noncreature cards in the graveyard")
    void etbIgnoresNoncreatureCards() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        Permanent siegfried = castAndResolveEtb();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(siegfried.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB mills only the available cards from a short library")
    void etbWithShortLibrary() {
        harness.setGraveyard(player1, List.of(new SiegfriedFamedSwordsman()));
        harness.setLibrary(player1, List.of(new SiegfriedFamedSwordsman(), new Forest()));

        Permanent siegfried = castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(siegfried.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB still adds counters with an empty library and ignores opposing graveyards")
    void etbWithEmptyLibraryIgnoresOpponentGraveyard() {
        harness.setGraveyard(player1, List.of(new SiegfriedFamedSwordsman()));
        harness.setGraveyard(player2, List.of(new SiegfriedFamedSwordsman(), new SiegfriedFamedSwordsman()));
        harness.setLibrary(player1, List.of());

        Permanent siegfried = castAndResolveEtb();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(siegfried.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB counts creatures in the graveyard at resolution rather than at entry")
    void etbCountsGraveyardAtResolution() {
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SiegfriedFamedSwordsman(), "{3}{B}");
        harness.passBothPriorities();
        Permanent siegfried = findPermanent(player1, "Siegfried, Famed Swordsman");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(siegfried.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setGraveyard(player1, List.of(new SiegfriedFamedSwordsman(), new SiegfriedFamedSwordsman()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(siegfried.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB still mills when Siegfried leaves before the trigger resolves")
    void etbStillMillsWithoutSource() {
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SiegfriedFamedSwordsman(), "{3}{B}");
        harness.passBothPriorities();
        Permanent siegfried = findPermanent(player1, "Siegfried, Famed Swordsman");
        harness.getPermanentRemovalService().removePermanentToHand(gd, siegfried);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertNotOnBattlefield(player1, "Siegfried, Famed Swordsman");
        harness.assertInHand(player1, "Siegfried, Famed Swordsman");
        assertThat(siegfried.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
