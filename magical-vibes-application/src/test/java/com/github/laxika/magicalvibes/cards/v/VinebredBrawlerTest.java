package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({VinebredBrawler.class, LlanowarElves.class, GrizzlyBears.class})
class VinebredBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger targets another Elf you control")
    void attackTriggerRestrictsTargets() {
        Permanent brawler = addCreatureReady(player1, new VinebredBrawler());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        Permanent nonElf = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentElf = addCreatureReady(player2, new LlanowarElves());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(elf.getId())
                .doesNotContain(brawler.getId(), nonElf.getId(), opponentElf.getId());
    }

    @Test
    @DisplayName("Attack trigger gives another Elf +2/+1 until end of turn")
    void attackTriggerBoostsElf() {
        addCreatureReady(player1, new VinebredBrawler());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, elf.getId());
        harness.passBothPriorities();

        assertThat(elf.getPowerModifier()).isEqualTo(2);
        assertThat(elf.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Vinebred Brawler must be blocked if able")
    void mustBeBlockedIfAble() {
        Permanent brawler = addCreatureReady(player1, new VinebredBrawler());
        brawler.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("One blocker satisfies Vinebred Brawler's requirement")
    void oneBlockerSuffices() {
        Permanent brawler = addCreatureReady(player1, new VinebredBrawler());
        brawler.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isFalse();
    }

    @Test
    void boostExpiresAtEndOfTurn() {
        addCreatureReady(player1, new VinebredBrawler());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, elf.getId());
        harness.passBothPriorities();

        assertThat(elf.getPowerModifier()).isEqualTo(2);
        assertThat(elf.getToughnessModifier()).isEqualTo(1);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(elf.getPowerModifier()).isZero();
        assertThat(elf.getToughnessModifier()).isZero();
    }

    @Test
    void tappedCreatureDoesNotHaveToBlock() {
        Permanent brawler = addCreatureReady(player1, new VinebredBrawler());
        brawler.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void triggerStillResolvesAfterBrawlerLeavesBattlefield() {
        Permanent brawler = addCreatureReady(player1, new VinebredBrawler());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, elf.getId());
        gd.playerBattlefields.get(player1.getId()).remove(brawler);
        harness.passBothPriorities();

        assertThat(elf.getPowerModifier()).isEqualTo(2);
        assertThat(elf.getToughnessModifier()).isEqualTo(1);
    }
}
