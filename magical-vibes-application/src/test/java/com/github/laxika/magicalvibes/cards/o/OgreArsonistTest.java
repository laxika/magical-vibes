package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OgreArsonistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys the target land")
    void etbDestroysTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new OgreArsonist()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID landId = harness.getPermanentId(player2, "Forest");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, landId, null);

        harness.passBothPriorities(); // creature resolves → ETB on stack
        harness.passBothPriorities(); // ETB resolves

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("ETB trigger goes on the stack targeting the chosen land")
    void etbTriggerTargetsLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new OgreArsonist()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID landId = harness.getPermanentId(player2, "Forest");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, landId, null);

        harness.passBothPriorities(); // creature resolves → ETB on stack

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ogre Arsonist"));
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(landId);
    }

    @Test
    @DisplayName("Only lands are legal targets for the ETB")
    void onlyLandsAreLegalTargets() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OgreArsonist()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, bearsId, null))
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
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ogre Arsonist");
    }
}
