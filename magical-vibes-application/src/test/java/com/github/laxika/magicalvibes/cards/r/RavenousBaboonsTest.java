package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RavenousBaboonsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys the target nonbasic land")
    void etbDestroysTargetNonbasicLand() {
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.setHand(player1, List.of(new RavenousBaboons()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");
        harness.castCreature(player1, 0, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ghost Quarter");
        harness.assertInGraveyard(player2, "Ghost Quarter");
    }

    @Test
    @DisplayName("ETB trigger targets the chosen nonbasic land")
    void etbTriggerTargetsChosenNonbasicLand() {
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.setHand(player1, List.of(new RavenousBaboons()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Basic lands are illegal ETB targets")
    void basicLandsAreIllegalTargets() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RavenousBaboons()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonbasic land");
    }
}
