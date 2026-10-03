package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.ErebosGodOfTheDead;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.ReturnedPhalanx;
import com.github.laxika.magicalvibes.cards.s.SatyrHedonist;
import com.github.laxika.magicalvibes.cards.s.ShipwreckSinger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkBetrayal.class, ReturnedPhalanx.class, SatyrHedonist.class, Forest.class,
        ShipwreckSinger.class, ErebosGodOfTheDead.class})
class DarkBetrayalTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a targeted black creature")
    void destroysBlackCreature() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new ReturnedPhalanx()).getId();

        harness.setHand(player1, List.of(new DarkBetrayal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target);

        harness.assertInGraveyard(player2, "Returned Phalanx");
    }

    @Test
    @DisplayName("Cannot target a nonblack creature")
    void cannotTargetNonblackCreature() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new SatyrHedonist()).getId();

        harness.setHand(player1, List.of(new DarkBetrayal()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        harness.setHand(player1, List.of(new DarkBetrayal()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys a multicolored creature that is black")
    void destroysMulticoloredBlackCreature() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new ShipwreckSinger()).getId();
        harness.setHand(player1, List.of(new DarkBetrayal()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target);

        harness.assertInGraveyard(player2, "Shipwreck Singer");
        harness.assertInGraveyard(player1, "Dark Betrayal");
    }

    @Test
    @DisplayName("Can destroy a black creature you control")
    void destroysOwnBlackCreature() {
        UUID target = harness.addToBattlefieldAndReturn(player1, new ReturnedPhalanx()).getId();
        harness.setHand(player1, List.of(new DarkBetrayal()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target);

        harness.assertInGraveyard(player1, "Returned Phalanx");
    }

    @Test
    @DisplayName("Cannot target a black enchantment that is not currently a creature")
    void cannotTargetBlackNoncreature() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new ErebosGodOfTheDead()).getId();
        harness.setHand(player1, List.of(new DarkBetrayal()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not destroy an indestructible black creature")
    void indestructibleCreatureSurvives() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new ErebosGodOfTheDead()).getId();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new ReturnedPhalanx());
        }
        harness.setHand(player1, List.of(new DarkBetrayal()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target);

        harness.assertOnBattlefield(player2, "Erebos, God of the Dead");
        harness.assertNotInGraveyard(player2, "Erebos, God of the Dead");
        harness.assertInGraveyard(player1, "Dark Betrayal");
    }
}
