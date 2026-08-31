package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BeastWalkers;
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

@CardUsed({SeaTroll.class, BeastWalkers.class})
class SeaTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Regenerates after being blocked by a blue creature")
    void regeneratesAfterBeingBlockedByBlueCreature() {
        Permanent troll = addCreatureReady(player1, new SeaTroll());
        troll.setAttacking(true);
        addCreatureReady(player2, new SeaTroll());

        declareBlock();

        activateRegeneration(player1);

        assertThat(findPermanent(player1, "Sea Troll").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regenerates after blocking a blue creature")
    void regeneratesAfterBlockingBlueCreature() {
        Permanent attacker = addCreatureReady(player1, new SeaTroll());
        attacker.setAttacking(true);
        addCreatureReady(player2, new SeaTroll());

        declareBlock();

        harness.forceActivePlayer(player2);
        activateRegeneration(player2);

        assertThat(findPermanent(player2, "Sea Troll").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot regenerate when the blocking creature was not blue")
    void cannotRegenerateAgainstNonBlueBlocker() {
        Permanent troll = addCreatureReady(player1, new SeaTroll());
        troll.setAttacking(true);
        addCreatureReady(player2, new BeastWalkers());

        declareBlock();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot regenerate when it was not in combat with a creature this turn")
    void cannotRegenerateWithoutCombat() {
        addCreatureReady(player1, new SeaTroll());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void remembersBlueCreatureAfterItLeavesBattlefield() {
        Permanent troll = addCreatureReady(player1, new SeaTroll());
        troll.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SeaTroll());

        declareBlock();
        gd.playerBattlefields.get(player2.getId()).remove(blocker);

        activateRegeneration(player1);

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void regenerationShieldSavesFromLethalDamage() {
        Permanent troll = addCreatureReady(player1, new SeaTroll());
        troll.setAttacking(true);
        addCreatureReady(player2, new SeaTroll());

        declareBlock();
        activateRegenerationDuringCombat(player1);
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(troll);
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isZero();
    }

    @Test
    void cannotRegenerateAfterTurnEnds() {
        Permanent troll = addCreatureReady(player1, new SeaTroll());
        troll.setAttacking(true);
        addCreatureReady(player2, new SeaTroll());

        declareBlock();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void declareBlock() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    private void activateRegeneration(Player player) {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.activateAbility(player, 0, null, null);
        harness.passBothPriorities();
    }

    private void activateRegenerationDuringCombat(Player player) {
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.activateAbility(player, 0, null, null);
        harness.passBothPriorities();
    }

}
