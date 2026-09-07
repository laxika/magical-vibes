package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuilledCharger.class, GrizzlyBears.class})
class QuilledChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 2 taps another creature and saddles Quilled Charger")
    void saddleTapsAnotherCreature() {
        Permanent charger = addCreatureReady(player1, new QuilledCharger());
        Permanent helper = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(charger.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled gives Quilled Charger +1/+2 and menace until end of turn")
    void attacksWhileSaddled() {
        Permanent charger = addCreatureReady(player1, new QuilledCharger());
        charger.setSaddled(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, charger)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, charger, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, charger)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, charger, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Attacking while not saddled does not trigger")
    void doesNotTriggerWhenNotSaddled() {
        Permanent charger = addCreatureReady(player1, new QuilledCharger());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, charger)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, charger, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent charger = addCreatureReady(player1, new QuilledCharger());

        declareAttackers(player1, List.of(0));
        charger.setSaddled(true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, charger)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, charger, Keyword.MENACE)).isFalse();
    }
}
