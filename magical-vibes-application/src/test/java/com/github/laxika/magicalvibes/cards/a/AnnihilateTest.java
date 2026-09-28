package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.ShivanZombie;
import com.github.laxika.magicalvibes.cards.y.YavimayaBarbarian;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Annihilate.class, Forest.class, ShivanZombie.class, YavimayaBarbarian.class})
class AnnihilateTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Annihilate destroys a nonblack creature and draws a card")
    void resolvingDestroysNonblackCreatureAndDraws() {
        Permanent barbarian = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.setHand(player1, List.of(new Annihilate()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, barbarian.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Yavimaya Barbarian");
        harness.assertInGraveyard(player2, "Yavimaya Barbarian");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Annihilate destroys the creature even with a regeneration shield")
    void cannotBeRegenerated() {
        Permanent barbarian = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        barbarian.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Annihilate()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, barbarian.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Yavimaya Barbarian");
        harness.assertInGraveyard(player2, "Yavimaya Barbarian");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new ShivanZombie());

        harness.setHand(player1, List.of(new Annihilate()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, zombie.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new Annihilate()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Does not draw if the target is removed before resolution")
    void doesNotDrawWhenTargetBecomesIllegal() {
        Permanent barbarian = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.setHand(player1, List.of(new Annihilate()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, barbarian.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Annihilate");
    }
}
