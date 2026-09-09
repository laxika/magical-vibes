package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OgreArsonist.class, Forest.class, OgreWarrior.class})
class OgreArsonistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys the target land")
    void etbDestroysTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new OgreArsonist()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID landId = harness.getPermanentId(player2, "Forest");
        harness.castCreature(player1, 0, landId);

        harness.passBothPriorities(); // creature resolves → ETB on stack
        harness.passBothPriorities(); // ETB resolves

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("ETB trigger goes on the stack targeting the chosen land")
    void etbTriggerTargetsLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new OgreArsonist()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID landId = harness.getPermanentId(player2, "Forest");
        harness.castCreature(player1, 0, landId);

        harness.passBothPriorities(); // creature resolves → ETB on stack

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Ogre Arsonist");
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(landId);
    }

    @Test
    @DisplayName("Only lands are legal targets for the ETB")
    void onlyLandsAreLegalTargets() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new OgreWarrior());
        harness.setHand(player1, List.of(new OgreArsonist()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID creatureId = harness.getPermanentId(player2, "Ogre Warrior");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    @Test
    @DisplayName("Can cast with no target when no lands exist")
    void canCastWithoutTargetWhenNoLands() {
        harness.setHand(player1, List.of(new OgreArsonist()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("ETB fizzles if the target land leaves before resolution")
    void etbFizzlesIfTargetLandLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new OgreArsonist()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID landId = harness.getPermanentId(player2, "Forest");
        harness.castCreature(player1, 0, landId);

        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
