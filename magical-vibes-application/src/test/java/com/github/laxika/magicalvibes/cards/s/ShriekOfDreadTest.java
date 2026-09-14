package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CalderaKavu;
import com.github.laxika.magicalvibes.cards.c.CavernHarpy;
import com.github.laxika.magicalvibes.cards.c.CrosissCatacombs;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShriekOfDread.class, CalderaKavu.class, CavernHarpy.class, CrosissCatacombs.class})
class ShriekOfDreadTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gains fear")
    void targetCreatureGainsFear() {
        Permanent bears = addCreature();
        setupSpell();

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent creature = addCreature(player2);
        setupSpell();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.hasKeyword(Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Fear wears off at end of turn")
    void fearWearsOffAtEndOfTurn() {
        Permanent bears = addCreature();
        setupSpell();

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CrosissCatacombs());
        setupSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Fear prevents a nonblack, nonartifact creature from blocking")
    void fearPreventsNonblackNonartifactCreatureFromBlocking() {
        Permanent attacker = addCreature(player1);
        setupSpell();

        harness.castAndResolveInstant(player1, 0, attacker.getId());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = addCreature(player2);
        blocker.setSummoningSick(false);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Fear allows a black creature to block")
    void fearAllowsBlackCreatureToBlock() {
        Permanent attacker = addCreature(player1);
        setupSpell();

        harness.castAndResolveInstant(player1, 0, attacker.getId());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CavernHarpy());
        blocker.setSummoningSick(false);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    private Permanent addCreature() {
        return addCreature(player1);
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new CalderaKavu());
    }

    private void setupSpell() {
        harness.setHand(player1, List.of(new ShriekOfDread()));
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
