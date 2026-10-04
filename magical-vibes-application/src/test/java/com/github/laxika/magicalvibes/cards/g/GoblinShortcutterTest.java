package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinShortcutter.class, CoralMerfolk.class})
class GoblinShortcutterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes target creature unable to block this turn")
    void etbMakesTargetUnableToBlock() {
        Permanent blocker = addCreatureReady(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new GoblinShortcutter()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, 0, blocker.getId());
        resolveAllTriggers();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Target creature cannot declare as blocker after ETB resolves")
    void targetCannotDeclareAsBlocker() {
        Permanent attacker = addCreatureReady(player1, new CoralMerfolk());
        Permanent blocker = addCreatureReady(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new GoblinShortcutter()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, 0, blocker.getId());
        resolveAllTriggers();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting onto an empty battlefield requires the ETB to target Shortcutter itself")
    void canCastWithoutTargetAndThenTargetItself() {
        harness.setHand(player1, List.of(new GoblinShortcutter()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Shortcutter");
        Permanent shortcutter = findPermanent(player1, "Goblin Shortcutter");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, shortcutter.getId());
        resolveAllTriggers();

        assertThat(shortcutter.isCantBlockThisTurn()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The blocking restriction ends when the turn ends")
    void blockingRestrictionExpires() {
        Permanent target = addCreatureReady(player1, new GoblinShortcutter());
        harness.setHand(player1, List.of(new GoblinShortcutter()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The ETB resolves independently of its source and affects only its target")
    void triggerSurvivesSourceLeaving() {
        Permanent target = addCreatureReady(player1, new GoblinShortcutter());
        Permanent other = addCreatureReady(player2, new GoblinShortcutter());
        harness.setHand(player1, List.of(new GoblinShortcutter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent source = gd.playerBattlefields.get(player1.getId()).getLast();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(other.isCantBlockThisTurn()).isFalse();
    }
}
