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

    @Test
    @DisplayName("Only another attacking creature is a legal target")
    void excludesStormAndNonAttackingCreatures() {
        addCreatureReady(player1, new StormQueenOfWakanda());
        Permanent attackingBears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(attackingBears.getId());

        harness.handlePermanentChosen(player1, attackingBears.getId());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, attackingBears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The attack boost evaluates power on resolution and then stays fixed")
    void boostUsesPowerAtResolution() {
        Permanent storm = addCreatureReady(player1, new StormQueenOfWakanda());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        storm.setPowerModifier(3);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(9);
        storm.setPowerModifier(0);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each flying attacker receives Storm's damage")
    void damagesEachFlyingAttacker() {
        addCreatureReady(player1, new StormQueenOfWakanda());
        Permanent first = addCreatureReady(player2, new AirElemental());
        Permanent second = addCreatureReady(player2, new AirElemental());
        first.setToughnessModifier(2);
        second.setToughnessModifier(2);

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getMarkedDamage()).isEqualTo(4);
        assertThat(second.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second);
    }
}
