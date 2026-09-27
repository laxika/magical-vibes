package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlabasterLeech;
import com.github.laxika.magicalvibes.cards.b.BlurredMongoose;
import com.github.laxika.magicalvibes.cards.c.CrystalSpray;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MetathranAerostat.class, AlabasterLeech.class, BlurredMongoose.class, CrystalSpray.class})
class MetathranAerostatTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a creature with mana value X onto the battlefield and returns the source")
    void putsExactManaValueCreatureAndReturnsSource() {
        harness.addToBattlefield(player1, new MetathranAerostat());
        harness.setHand(player1, List.of(new AlabasterLeech(), new BlurredMongoose()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Blurred Mongoose");
        harness.assertInHand(player1, "Metathran Aerostat");
        harness.assertInHand(player1, "Alabaster Leech");
    }

    @Test
    @DisplayName("Does not offer a creature whose mana value differs from X")
    void requiresExactManaValue() {
        harness.addToBattlefield(player1, new MetathranAerostat());
        harness.setHand(player1, List.of(new AlabasterLeech()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Alabaster Leech");
        harness.assertOnBattlefield(player1, "Metathran Aerostat");
        harness.assertInHand(player1, "Alabaster Leech");
    }

    @Test
    @DisplayName("Declining the optional creature leaves the source and card in place")
    void mayDecline() {
        harness.addToBattlefield(player1, new MetathranAerostat());
        harness.setHand(player1, List.of(new BlurredMongoose()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertOnBattlefield(player1, "Metathran Aerostat");
        harness.assertInHand(player1, "Blurred Mongoose");
    }

    @Test
    @DisplayName("Does not offer a noncreature card even when its mana value matches X")
    void requiresCreatureCard() {
        harness.addToBattlefield(player1, new MetathranAerostat());
        harness.setHand(player1, List.of(new CrystalSpray()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Metathran Aerostat");
        harness.assertInHand(player1, "Crystal Spray");
    }

    @Test
    @DisplayName("Pays X and blue mana without requiring the source to tap")
    void paysManaWithoutTappingSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MetathranAerostat());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(source.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
