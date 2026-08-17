package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IcatianSkirmishersTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking in a band grants first strike to the other band member")
    void grantsFirstStrikeToBandmate() {
        addReadyCreature(player1, new IcatianSkirmishers());
        Permanent bandmate = addReadyCreature(player1, new GrizzlyBears());
        Permanent loner = addReadyCreature(player1, new GrizzlyBears());

        declareAttackersInBand();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bandmate, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, loner, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The trigger does not grant first strike to an attacker outside the band")
    void doesNotGrantFirstStrikeOutsideBand() {
        addReadyCreature(player1, new IcatianSkirmishers());
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());

        declareAttackersWithoutBand();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The granted first strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        addReadyCreature(player1, new IcatianSkirmishers());
        Permanent bandmate = addReadyCreature(player1, new GrizzlyBears());

        declareAttackersInBand();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bandmate, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bandmate, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void declareAttackersInBand() {
        declareAttackers(List.of(0, 1), List.of(List.of(0, 1)));
    }

    private void declareAttackersWithoutBand() {
        declareAttackers(List.of(0, 1), null);
    }

    private void declareAttackers(List<Integer> attackers, List<List<Integer>> bands) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, attackers, null, bands));
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
