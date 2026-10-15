package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuangZhongShuGeneral.class, ShuFootSoldiers.class})
class HuangZhongShuGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Huang Zhong can be blocked by one creature")
    void canBeBlockedByOneCreature() {
        Permanent attacker = addCreatureReady(player1, new HuangZhongShuGeneral());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new ShuFootSoldiers());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Huang Zhong cannot be blocked by two creatures")
    void cannotBeBlockedByTwoCreatures() {
        Permanent attacker = addCreatureReady(player1, new HuangZhongShuGeneral());
        attacker.setAttacking(true);

        addCreatureReady(player2, new ShuFootSoldiers());
        addCreatureReady(player2, new ShuFootSoldiers());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Huang Zhong's restriction does not limit blockers for another attacker")
    void restrictionDoesNotApplyToOtherAttackers() {
        Permanent firstAttacker = addCreatureReady(player1, new HuangZhongShuGeneral());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new ShuFootSoldiers());
        secondAttacker.setAttacking(true);

        Permanent firstBlocker = addCreatureReady(player2, new ShuFootSoldiers());
        Permanent secondBlocker = addCreatureReady(player2, new ShuFootSoldiers());
        Permanent thirdBlocker = addCreatureReady(player2, new ShuFootSoldiers());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1),
                new BlockerAssignment(2, 1)
        ));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
        assertThat(thirdBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Huang Zhong may go unblocked even when the defender has a creature")
    void canGoUnblocked() {
        addCreatureReady(player1, new HuangZhongShuGeneral());
        addCreatureReady(player2, new ShuFootSoldiers());
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }
}
