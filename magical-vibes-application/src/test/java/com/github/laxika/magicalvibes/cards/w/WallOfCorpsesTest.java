package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfCorpses.class, FemerefScouts.class})
class WallOfCorpsesTest extends BaseCardTest {

    @Test
    @DisplayName("{B}, Sacrifice: destroys the creature Wall of Corpses is blocking")
    void destroysBlockedCreature() {
        Permanent attacker = addCreatureReady(player1, new FemerefScouts());
        addCreatureReady(player2, new WallOfCorpses());

        blockWithWall();
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Femeref Scouts");
        harness.assertInGraveyard(player2, "Wall of Corpses");
    }

    @Test
    @DisplayName("Ability cannot target a creature Wall of Corpses isn't blocking")
    void cannotTargetUnblockedCreature() {
        addCreatureReady(player1, new FemerefScouts());
        Permanent otherAttacker = addCreatureReady(player1, new FemerefScouts());
        addCreatureReady(player2, new WallOfCorpses());
        otherAttacker.setAttacking(true);

        blockWithWall();
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, otherAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without black mana")
    void cannotActivateWithoutBlackMana() {
        Permanent attacker = addCreatureReady(player1, new FemerefScouts());
        addCreatureReady(player2, new WallOfCorpses());

        blockWithWall();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertOnBattlefield(player2, "Wall of Corpses");
    }

    /** Declares player1's first creature as an attacker and blocks it with player2's Wall of Corpses. */
    private void blockWithWall() {
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
