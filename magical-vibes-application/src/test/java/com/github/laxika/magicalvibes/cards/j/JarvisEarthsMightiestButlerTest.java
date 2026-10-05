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

    @Test
    void drawResolvesBeforeTheHeroSpell() {
        harness.addToBattlefield(player1, new JarvisEarthsMightiestButler());
        harness.setLibrary(player1, List.of(new JarvisEarthsMightiestButler()));
        harness.setHand(player1, List.of(new CaptainAmericaWingsOfFreedom()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement().isInstanceOf(JarvisEarthsMightiestButler.class);
        harness.assertNotOnBattlefield(player1, "Captain America, Wings of Freedom");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Captain America, Wings of Freedom");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void opponentsHeroSpellDoesNotTriggerJarvis() {
        harness.addToBattlefield(player2, new JarvisEarthsMightiestButler());
        harness.setLibrary(player2, List.of(new JarvisEarthsMightiestButler()));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new CaptainAmericaWingsOfFreedom()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Captain America, Wings of Freedom");
    }

    @Test
    void heroEnteringWithoutBeingCastDoesNotDraw() {
        harness.addToBattlefield(player1, new JarvisEarthsMightiestButler());
        harness.setLibrary(player1, List.of(new JarvisEarthsMightiestButler()));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new CaptainAmericaWingsOfFreedom());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Captain America, Wings of Freedom");
    }
}
