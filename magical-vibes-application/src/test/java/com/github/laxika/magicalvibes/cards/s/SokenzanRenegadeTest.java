package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SokenzanRenegade.class, SakuraTribeScout.class})
class SokenzanRenegadeTest extends BaseCardTest {

    @Test
    @DisplayName("Bushido 1 triggers when Sokenzan Renegade becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent renegade = addReadyRenegade(player1);
        addReadySupportCreature(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(renegade.getPowerModifier()).isEqualTo(1);
        assertThat(renegade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bushido 1 triggers when Sokenzan Renegade blocks")
    void blocksGetsBushidoBonus() {
        addReadySupportCreature(player1);
        Permanent renegade = addReadyRenegade(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(renegade.getPowerModifier()).isEqualTo(1);
        assertThat(renegade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The player with the most cards in hand gains control during upkeep")
    void playerWithMostCardsInHandGainsControl() {
        harness.setHand(player1, List.of(new SakuraTribeScout()));
        harness.setHand(player2, List.of(new SakuraTribeScout(), new SakuraTribeScout(), new SakuraTribeScout()));
        harness.addToBattlefield(player1, new SokenzanRenegade());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sokenzan Renegade");
        harness.assertOnBattlefield(player2, "Sokenzan Renegade");
    }

    @Test
    @DisplayName("Sokenzan Renegade does not change control when hand sizes are tied")
    void noChangeOnTiedHandSizes() {
        harness.setHand(player1, List.of(new SakuraTribeScout(), new SakuraTribeScout()));
        harness.setHand(player2, List.of(new SakuraTribeScout(), new SakuraTribeScout()));
        harness.addToBattlefield(player1, new SokenzanRenegade());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sokenzan Renegade");
        harness.assertNotOnBattlefield(player2, "Sokenzan Renegade");
    }

    @Test
    @DisplayName("The upkeep ability does nothing if the hand-size lead disappears before resolution")
    void noControlChangeWhenHandLeadDisappearsBeforeResolution() {
        harness.setHand(player1, List.of(new SakuraTribeScout()));
        harness.setHand(player2, List.of(new SakuraTribeScout(), new SakuraTribeScout()));
        harness.addToBattlefield(player1, new SokenzanRenegade());

        advanceToUpkeep(player1);
        harness.setHand(player2, List.of(new SakuraTribeScout()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sokenzan Renegade");
        harness.assertNotOnBattlefield(player2, "Sokenzan Renegade");
    }

    @Test
    @DisplayName("Bushido 1 wears off at the end of the turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent renegade = addReadyRenegade(player1);
        addReadySupportCreature(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(renegade.getPowerModifier()).isEqualTo(1);
        assertThat(renegade.getToughnessModifier()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(renegade.getPowerModifier()).isZero();
        assertThat(renegade.getToughnessModifier()).isZero();
    }

    private Permanent addReadyRenegade(Player player) {
        return addCreatureReady(player, new SokenzanRenegade());
    }

    private Permanent addReadySupportCreature(Player player) {
        return addCreatureReady(player, new SakuraTribeScout());
    }
}
