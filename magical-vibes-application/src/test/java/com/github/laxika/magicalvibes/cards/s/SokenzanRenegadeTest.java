package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SokenzanRenegadeTest extends BaseCardTest {

    @Test
    @DisplayName("Bushido 1 triggers when Sokenzan Renegade becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent renegade = addReadyRenegade(player1);
        renegade.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(renegade.getPowerModifier()).isEqualTo(1);
        assertThat(renegade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bushido 1 triggers when Sokenzan Renegade blocks")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);
        Permanent renegade = addReadyRenegade(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(renegade.getPowerModifier()).isEqualTo(1);
        assertThat(renegade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The player with the most cards in hand gains control during upkeep")
    void playerWithMostCardsInHandGainsControl() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new SokenzanRenegade());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sokenzan Renegade");
        harness.assertOnBattlefield(player2, "Sokenzan Renegade");
    }

    @Test
    @DisplayName("Sokenzan Renegade does not change control when hand sizes are tied")
    void noChangeOnTiedHandSizes() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new SokenzanRenegade());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sokenzan Renegade");
        harness.assertNotOnBattlefield(player2, "Sokenzan Renegade");
    }

    private Permanent addReadyRenegade(Player player) {
        Permanent permanent = new Permanent(new SokenzanRenegade());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyBears(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
