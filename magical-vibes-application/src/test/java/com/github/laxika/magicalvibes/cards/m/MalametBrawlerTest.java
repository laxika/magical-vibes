package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MalametBrawler.class, GrizzlyBears.class})
class MalametBrawlerTest extends BaseCardTest {

    @Test
    void grantsTrampleToAnotherAttackingCreature() {
        Permanent brawler = addReadyCreature(player1, new MalametBrawler());
        Permanent otherAttacker = addReadyCreature(player1, new GrizzlyBears());

        declareMalametAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, brawler, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void canTargetItself() {
        Permanent brawler = addReadyCreature(player1, new MalametBrawler());
        addReadyCreature(player1, new GrizzlyBears());

        declareMalametAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, brawler.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, brawler, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void cannotTargetNonAttackingCreature() {
        addReadyCreature(player1, new MalametBrawler());
        addReadyCreature(player1, new GrizzlyBears());
        Permanent nonAttacker = addReadyCreature(player1, new GrizzlyBears());

        declareMalametAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedTrampleWearsOffAtEndOfTurn() {
        addReadyCreature(player1, new MalametBrawler());
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());

        declareMalametAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareMalametAttackers(List<Integer> attackers) {
        declareAttackers(player1, attackers);
    }
}
