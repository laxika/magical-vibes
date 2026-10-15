package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VoraciousVermin;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChargingHooligan.class, GrizzlyBears.class, VoraciousVermin.class, Unsummon.class})
class ChargingHooliganTest extends BaseCardTest {

    @Test
    @DisplayName("Counts remaining attackers and checks for a Rat when the trigger resolves")
    void usesAttackingCreaturesAtResolution() {
        var hooligan = addCreatureReady(player1, new ChargingHooligan());
        var rat = addCreatureReady(player1, new VoraciousVermin());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.passPriority(player1);
            harness.castInstant(player2, 0, rat.getId());
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, hooligan)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, hooligan, Keyword.TRAMPLE)).isFalse();
        harness.assertNotOnBattlefield(player1, "Voracious Vermin");
        harness.assertInHand(player1, "Voracious Vermin");
    }

    @Test
    @DisplayName("Gets +1/+0 for each attacking creature, including itself")
    void getsPowerForEachAttackingCreature() {
        var hooligan = addCreatureReady(player1, new ChargingHooligan());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1, 2));
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, hooligan)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, hooligan)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, hooligan, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gains trample when a Rat is attacking")
    void gainsTrampleWhenRatAttacks() {
        var hooligan = addCreatureReady(player1, new ChargingHooligan());
        addCreatureReady(player1, new VoraciousVermin());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            resolveAllTriggers();
        });

        assertThat(gqs.hasKeyword(gd, hooligan, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The attack bonuses wear off at end of turn")
    void bonusesWearOffAtEndOfTurn() {
        var hooligan = addCreatureReady(player1, new ChargingHooligan());
        addCreatureReady(player1, new VoraciousVermin());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            resolveAllTriggers();
        });
        assertThat(gqs.getEffectivePower(gd, hooligan)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, hooligan, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hooligan)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, hooligan, Keyword.TRAMPLE)).isFalse();
    }
}
