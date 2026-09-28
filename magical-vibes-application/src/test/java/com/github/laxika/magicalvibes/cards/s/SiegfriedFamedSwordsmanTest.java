package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SiegfriedFamedSwordsman.class, Forest.class, GrizzlyBears.class})
class SiegfriedFamedSwordsmanTest extends BaseCardTest {

    private Permanent castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SiegfriedFamedSwordsman()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Siegfried, Famed Swordsman");
    }

    @Test
    @DisplayName("ETB mills three cards and puts twice the creature count in counters")
    void etbMillsAndDoublesCreatureCount() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears()));

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
}
