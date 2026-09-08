package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EasyPrey.class, Forest.class, GrayOgre.class, GrizzlyBears.class})
class EasyPreyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature with mana value 2 or less")
    void destroysSmallCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EasyPrey()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature with mana value greater than 2")
    void cannotTargetLargerCreature() {
        harness.addToBattlefield(player2, new GrayOgre());
        harness.setHand(player1, List.of(new EasyPrey()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Gray Ogre")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 2 or less");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new EasyPrey()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with mana value 2 or less");
    }

    @Test
    @DisplayName("Cycling {2} discards Easy Prey and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new EasyPrey()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Easy Prey");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
