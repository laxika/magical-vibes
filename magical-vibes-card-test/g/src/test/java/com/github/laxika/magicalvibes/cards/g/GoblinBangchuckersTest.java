package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinBangchuckers.class, RuneclawBear.class, Unsummon.class})
class GoblinBangchuckersTest extends BaseCardTest {

    @Test
    @DisplayName("Winning the flip deals 2 to the targeted player, losing deals 2 to itself")
    void flipDamagesTargetOrSelf() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GoblinBangchuckers());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        if (wonFlip()) {
            harness.assertLife(player2, 18);
            harness.assertOnBattlefield(player1, "Goblin Bangchuckers");
        } else {
            harness.assertLife(player2, 20);
            // 2 damage kills the 2/2 itself.
            harness.assertNotOnBattlefield(player1, "Goblin Bangchuckers");
            harness.assertInGraveyard(player1, "Goblin Bangchuckers");
        }
    }

    @Test
    @DisplayName("A won flip kills the targeted 2/2; a lost flip leaves it alive")
    void flipDamagesTargetCreatureOnlyOnWin() {
        addCreatureReady(player1, new GoblinBangchuckers());
        harness.addToBattlefield(player2, new RuneclawBear());

        UUID targetId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        if (wonFlip()) {
            harness.assertInGraveyard(player2, "Runeclaw Bear");
            harness.assertOnBattlefield(player1, "Goblin Bangchuckers");
        } else {
            harness.assertOnBattlefield(player2, "Runeclaw Bear");
            harness.assertInGraveyard(player1, "Goblin Bangchuckers");
        }
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new GoblinBangchuckers());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Activation pays the tap cost before flipping on resolution")
    void tapsBeforeFlipping() {
        Permanent source = addCreatureReady(player1, new GoblinBangchuckers());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("coin flip for Goblin Bangchuckers"));
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("coin flip for Goblin Bangchuckers"));
        harness.assertLife(player2, wonFlip() ? 18 : 20);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent source = addCreatureReady(player1, new GoblinBangchuckers());
        source.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Targeting itself deals lethal damage regardless of the coin flip")
    void canTargetItself() {
        Permanent source = addCreatureReady(player1, new GoblinBangchuckers());

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Bangchuckers");
        harness.assertInGraveyard(player1, "Goblin Bangchuckers");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An illegal target prevents both the coin flip and self damage")
    void returnedTargetPreventsCoinFlip() {
        Permanent source = addCreatureReady(player1, new GoblinBangchuckers());
        harness.addToBattlefield(player2, new RuneclawBear());
        UUID targetId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.assertInHand(player2, "Runeclaw Bear");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Bangchuckers");
        assertThat(source.getMarkedDamage()).isZero();
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("coin flip for Goblin Bangchuckers"));
    }

    @Test
    @DisplayName("Removing the source does not stop its ability from resolving")
    void resolvesAfterSourceReturnsToHand() {
        Permanent source = addCreatureReady(player1, new GoblinBangchuckers());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.assertInHand(player1, "Goblin Bangchuckers");
        harness.passBothPriorities();

        harness.assertLife(player2, wonFlip() ? 18 : 20);
        harness.assertInHand(player1, "Goblin Bangchuckers");
        harness.assertNotOnBattlefield(player1, "Goblin Bangchuckers");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("coin flip for Goblin Bangchuckers"));
    }

    private boolean wonFlip() {
        return gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip for Goblin Bangchuckers"));
    }

}
