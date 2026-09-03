package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.b.BalduvianConjurer;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fumarole.class, BalduvianBears.class, SnowCoveredForest.class, BalduvianConjurer.class})
class FumaroleTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys both the targeted creature and the targeted land, and costs 3 life")
    void destroysCreatureAndLand() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        harness.setHand(player1, List.of(new Fumarole()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, List.of(bear.getId(), forest.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Balduvian Bears");
        harness.assertInGraveyard(player2, "Snow-Covered Forest");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Cannot be cast without enough life to pay the additional cost")
    void cannotBeCastWithoutEnoughLife() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        harness.setHand(player1, List.of(new Fumarole()));
        harness.setLife(player1, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bear.getId(), forest.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Balduvian Bears");
        harness.assertOnBattlefield(player2, "Snow-Covered Forest");
        harness.assertLife(player1, 2);
    }

    @Test
    @DisplayName("Second target must be a land")
    void secondTargetMustBeLand() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new Fumarole()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponentBear.getId(), ownBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First target must be a creature")
    void firstTargetMustBeCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new Fumarole()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(forest.getId(), bear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        harness.assertOnBattlefield(player2, "Snow-Covered Forest");
        harness.assertOnBattlefield(player2, "Balduvian Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Destroys the remaining legal target when the other target leaves before resolution")
    void destroysRemainingTargetWhenOtherTargetLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        harness.setHand(player1, List.of(new Fumarole()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, List.of(bear.getId(), forest.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(bear);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Snow-Covered Forest");
        harness.assertInGraveyard(player2, "Snow-Covered Forest");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Can use the same creature land for both target requirements")
    void canUseSameCreatureLandForBothTargetRequirements() {
        addCreatureReady(player1, new BalduvianConjurer());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Fumarole()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, List.of(forest.getId(), forest.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Snow-Covered Forest");
        harness.assertLife(player1, 17);
    }
}
