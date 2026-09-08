package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KothFireOfResistanceTest extends BaseCardTest {

    @Test
    @DisplayName("+2 searches for a basic Mountain and puts it into hand")
    void plusTwoSearchesForBasicMountain() {
        Permanent koth = addReadyKoth(player1, 4);
        Card mountain = new Mountain();
        harness.setLibrary(player1, List.of(mountain, new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(mountain);
        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-3 deals damage equal to the Mountains controlled")
    void minusThreeDealsDamageEqualToMountains() {
        Permanent koth = addReadyKoth(player1, 4);
        addMountain(player1);
        addMountain(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ultimate creates a landfall damage emblem")
    void ultimateEmblemDealsDamageWhenMountainEnters() {
        addReadyKoth(player1, 7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Mountain()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("The ultimate emblem ignores non-Mountain lands")
    void ultimateEmblemIgnoresNonMountainLands() {
        addReadyKoth(player1, 7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent addReadyKoth(Player player, int loyalty) {
        Permanent perm = new Permanent(new KothFireOfResistance());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void addMountain(Player player) {
        gd.playerBattlefields.get(player.getId()).add(new Permanent(new Mountain()));
    }
}
