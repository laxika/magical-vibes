package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormQueenOfWakanda.class, GrizzlyBears.class, AirElemental.class})
class StormQueenOfWakandaTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking grants another attacking creature flying and +X/+0")
    void boostsAnotherAttackingCreatureByStormsPower() {
        Permanent storm = addCreatureReady(player1, new StormQueenOfWakanda());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, storm)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The attack boost uses Storm's current power")
    void attackBoostUsesCurrentPower() {
        Permanent storm = addCreatureReady(player1, new StormQueenOfWakanda());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        storm.setPowerModifier(2);

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(8);
    }

    @Test
    @DisplayName("Storm deals damage equal to her power to a flying creature attacking her")
    void damagesFlyingCreatureAttackingDirectly() {
        Permanent storm = addCreatureReady(player1, new StormQueenOfWakanda());
        Permanent attacker = addCreatureReady(player2, new AirElemental());
        attacker.setToughnessModifier(2);
        storm.setPowerModifier(2);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("A non-flying creature does not trigger Storm's damage ability")
    void doesNotDamageNonFlyingAttacker() {
        addCreatureReady(player1, new StormQueenOfWakanda());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setToughnessModifier(4);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isZero();
    }
}
