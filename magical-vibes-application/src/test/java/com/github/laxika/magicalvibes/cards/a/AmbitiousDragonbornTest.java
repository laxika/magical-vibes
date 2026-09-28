package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmbitiousDragonborn.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class AmbitiousDragonbornTest extends BaseCardTest {

    @Test
    void entersWithGreatestPowerAmongControlledCreatures() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        Permanent dragonborn = castDragonborn();

        assertThat(dragonborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void usesGreatestCreaturePowerFromOwnGraveyardAndIgnoresOtherCardsAndPlayers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new HillGiant(), new Shock()));
        harness.setGraveyard(player2, List.of(new HillGiant()));

        Permanent dragonborn = castDragonborn();

        assertThat(dragonborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private Permanent castDragonborn() {
        harness.setHand(player1, List.of(new AmbitiousDragonborn()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Ambitious Dragonborn");
    }
}
