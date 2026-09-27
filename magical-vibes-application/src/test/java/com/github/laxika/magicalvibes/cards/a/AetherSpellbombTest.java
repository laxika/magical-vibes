package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherSpellbomb.class, YotianSoldier.class})
class AetherSpellbombTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the blue ability returns a target creature to its owner's hand")
    void returnsTargetCreatureToOwnersHand() {
        harness.addToBattlefield(player1, new AetherSpellbomb());
        var target = harness.addToBattlefieldAndReturn(player2, new YotianSoldier());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Aether Spellbomb");
        harness.assertInGraveyard(player1, "Aether Spellbomb");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Yotian Soldier");
        harness.assertInHand(player2, "Yotian Soldier");
    }

    @Test
    @DisplayName("Resolving the colorless ability draws a card")
    void drawsACard() {
        harness.addToBattlefield(player1, new AetherSpellbomb());
        harness.setLibrary(player1, List.of(new YotianSoldier()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aether Spellbomb");
        harness.assertInHand(player1, "Yotian Soldier");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("The blue ability cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new AetherSpellbomb());
        var target = harness.addToBattlefieldAndReturn(player2, new AetherSpellbomb());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");

        harness.assertOnBattlefield(player1, "Aether Spellbomb");
    }

    @Test
    @DisplayName("Returns the target creature to its owner's hand when its owner differs from its controller")
    void returnsTargetToItsOwnersHandWhenOwnerDiffersFromController() {
        harness.addToBattlefield(player1, new AetherSpellbomb());
        var targetCard = new YotianSoldier();
        targetCard.setOwnerId(player1.getId());
        var target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Yotian Soldier");
        harness.assertInHand(player1, "Yotian Soldier");
        harness.assertNotInHand(player2, "Yotian Soldier");
    }

    @Test
    @DisplayName("Does nothing if the target creature leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new AetherSpellbomb());
        var target = harness.addToBattlefieldAndReturn(player2, new YotianSoldier());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player2, "Yotian Soldier");
        harness.assertInGraveyard(player1, "Aether Spellbomb");
    }
}
