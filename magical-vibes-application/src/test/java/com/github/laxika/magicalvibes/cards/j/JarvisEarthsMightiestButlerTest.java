package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CaptainAmericaWingsOfFreedom;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JarvisEarthsMightiestButler.class, CaptainAmericaWingsOfFreedom.class, GrizzlyBears.class})
class JarvisEarthsMightiestButlerTest extends BaseCardTest {

    @Test
    void castingAHeroSpellDrawsACard() {
        harness.addToBattlefield(player1, new JarvisEarthsMightiestButler());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CaptainAmericaWingsOfFreedom()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement().isInstanceOf(GrizzlyBears.class);
    }

    @Test
    void castingANonHeroSpellDoesNotDrawACard() {
        harness.addToBattlefield(player1, new JarvisEarthsMightiestButler());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
