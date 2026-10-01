package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.g.GruulTurf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OgreSavant.class, GhostWarden.class, GruulTurf.class})
class OgreSavantTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the target creature when blue mana was spent")
    void returnsTargetCreatureWhenBlueManaWasSpent() {
        var target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        harness.setHand(player1, List.of(new OgreSavant()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ghost Warden");
        harness.assertInHand(player2, "Ghost Warden");
    }

    @Test
    @DisplayName("Chooses a creature when blue mana was spent and no target was supplied")
    void choosesTargetWhenBlueManaWasSpentWithoutSuppliedTarget() {
        var target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        harness.setHand(player1, List.of(new OgreSavant()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ghost Warden");
        harness.assertInHand(player2, "Ghost Warden");
    }

    @Test
    @DisplayName("Does not return the target creature when no blue mana was spent")
    void doesNotReturnTargetCreatureWithoutBlueMana() {
        var target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        harness.setHand(player1, List.of(new OgreSavant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ghost Warden");
    }

    @Test
    @DisplayName("Does not require a target when no blue mana was spent")
    void doesNotRequireTargetWithoutBlueMana() {
        harness.setHand(player1, List.of(new OgreSavant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ogre Savant");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target only a creature")
    void canTargetOnlyCreature() {
        var target = harness.addToBattlefieldAndReturn(player2, new GruulTurf());
        harness.setHand(player1, List.of(new OgreSavant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
