package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.h.HeadlessSkaab;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NephaliaSeakite.class, HeadlessSkaab.class})
class NephaliaSeakiteTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows Nephalia Seakite to be cast during an opponent's combat")
    void flashAllowsCastingDuringOpponentsCombat() {
        harness.setHand(player1, List.of(new NephaliaSeakite()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nephalia Seakite");
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Nephalia Seakite")
    void groundCreatureCannotBlock() {
        addCreatureReady(player1, new NephaliaSeakite());
        addCreatureReady(player2, new HeadlessSkaab());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Another flying creature can block Nephalia Seakite")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new NephaliaSeakite());
        Permanent blocker = addCreatureReady(player2, new NephaliaSeakite());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Nephalia Seakite can block a ground attacker immediately after being cast")
    void flashedCreatureCanBlockDespiteSummoningSickness() {
        addCreatureReady(player2, new HeadlessSkaab());
        harness.setHand(player1, List.of(new NephaliaSeakite()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        harness.castCreature(player1, 0);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        Permanent blocker = findPermanent(player1, "Nephalia Seakite");
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
