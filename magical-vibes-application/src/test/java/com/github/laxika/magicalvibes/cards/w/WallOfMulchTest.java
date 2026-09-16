package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfMulch.class, Forest.class, ElvishWarrior.class})
class WallOfMulchTest extends BaseCardTest {

    @Test
    @DisplayName("{G}, Sacrifice a Wall: sacrifices itself and draws a card")
    void sacrificesItselfAndDraws() {
        harness.addToBattlefield(player1, new WallOfMulch());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wall of Mulch");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A non-Wall creature is not a legal sacrifice; the Wall itself is used")
    void nonWallCreatureIsNotSacrificed() {
        harness.addToBattlefield(player1, new WallOfMulch());
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elvish Warrior");
        harness.assertNotOnBattlefield(player1, "Wall of Mulch");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("{G}, Sacrifice a Wall: can sacrifice another Wall instead of its source")
    void canSacrificeAnotherWall() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new WallOfMulch());
        Permanent otherWall = harness.addToBattlefieldAndReturn(player1, new WallOfMulch());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, otherWall.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Ability cannot be activated without {G}")
    void requiresGreenMana() {
        harness.addToBattlefield(player1, new WallOfMulch());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Wall of Mulch");
    }
}
