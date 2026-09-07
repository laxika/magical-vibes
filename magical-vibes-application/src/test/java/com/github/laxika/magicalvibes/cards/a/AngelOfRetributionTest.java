package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({AngelOfRetribution.class, GrizzlyBears.class})
class AngelOfRetributionTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Angel of Retribution")
    void flyingPreventsGroundBlocker() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent angel = addCreatureReady(player1, new AngelOfRetribution());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(angel);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("First strike defeats an equally sized creature before it deals combat damage")
    void firstStrikeDealsDamageBeforeRegularCombatDamage() {
        Card attackerCard = new Card();
        attackerCard.setName("Five Power Creature");
        attackerCard.setType(CardType.CREATURE);
        attackerCard.setManaCost("{5}");
        attackerCard.setColor(CardColor.GREEN);
        attackerCard.setPower(5);
        attackerCard.setToughness(5);

        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent angel = new Permanent(new AngelOfRetribution());
        angel.setSummoningSick(false);
        angel.setBlocking(true);
        angel.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(angel);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(angel);
    }
}
