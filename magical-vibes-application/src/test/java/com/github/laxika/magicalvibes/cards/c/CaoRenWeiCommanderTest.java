package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.cards.w.WuInfantry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaoRenWeiCommander.class, ShuCavalry.class, WuInfantry.class})
class CaoRenWeiCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB causes controller to lose 3 life")
    void etbLosesThreeLife() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new CaoRenWeiCommander(), "{2}{B}{B}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Cao Ren, Wei Commander");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Horsemanship prevents a creature without horsemanship from blocking Cao Ren")
    void cannotBeBlockedByCreatureWithoutHorsemanship() {
        Permanent blocker = addCreatureReady(player2, new WuInfantry());
        Permanent caoRen = addCreatureReady(player1, new CaoRenWeiCommander());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(caoRen)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    @DisplayName("A creature with horsemanship can block Cao Ren")
    void canBeBlockedByCreatureWithHorsemanship() {
        Permanent blocker = addCreatureReady(player2, new ShuCavalry());
        Permanent caoRen = addCreatureReady(player1, new CaoRenWeiCommander());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(caoRen))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Life loss waits for the ETB trigger to resolve and survives the source leaving")
    void lifeLossResolvesAfterSourceLeaves() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new CaoRenWeiCommander(), "{2}{B}{B}");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Cao Ren, Wei Commander");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        Permanent caoRen = findPermanent(player1, "Cao Ren, Wei Commander");
        gd.playerBattlefields.get(player1.getId()).remove(caoRen);
        gd.playerGraveyards.get(player1.getId()).add(caoRen.getCard());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cao Ren entering for the opposing player makes only that controller lose life")
    void opposingControllerLosesLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new CaoRenWeiCommander(), "{2}{B}{B}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Cao Ren, Wei Commander");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }
}
