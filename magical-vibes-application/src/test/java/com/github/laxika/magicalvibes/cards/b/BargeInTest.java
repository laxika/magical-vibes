package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BargeIn.class, EliteVanguard.class, GrizzlyBears.class})
class BargeInTest extends BaseCardTest {

    @Test
    @DisplayName("Pumps the target and grants trample to attacking non-Humans")
    void pumpsTargetAndGrantsTrampleToAttackingNonHumans() {
        Permanent target = addAttacker(player1, player2, new GrizzlyBears());
        Permanent attackingHuman = addAttacker(player2, player1, new EliteVanguard());
        Permanent idleCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BargeIn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attackingHuman, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, idleCreature, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent permanent = addCreatureReady(controller, card);
        permanent.setAttacking(true);
        permanent.setAttackTarget(defender.getId());
        return permanent;
    }
}
