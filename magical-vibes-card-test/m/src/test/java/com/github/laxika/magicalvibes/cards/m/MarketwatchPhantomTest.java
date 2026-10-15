package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CaseOfTheGatewayExpress;
import com.github.laxika.magicalvibes.cards.g.GraniteWitness;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarketwatchPhantom.class, GraniteWitness.class, CaseOfTheGatewayExpress.class})
class MarketwatchPhantomTest extends BaseCardTest {

    @Test
    @DisplayName("Gains flying when another creature you control with power 2 or less enters")
    void gainsFlyingForSmallAlly() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new MarketwatchPhantom());

        harness.setHand(player1, List.of(new MarketwatchPhantom()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, phantom, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new MarketwatchPhantom());

        harness.setHand(player1, List.of(new MarketwatchPhantom()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, phantom, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger for a creature with power greater than 2")
    void doesNotTriggerForHighPowerAlly() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new MarketwatchPhantom());

        harness.setHand(player1, List.of(new GraniteWitness()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, phantom, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature")
    void doesNotTriggerForOpponentsCreature() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new MarketwatchPhantom());

        harness.enterBattlefieldAndReturn(player2, new MarketwatchPhantom());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, phantom, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger for its own entry")
    void doesNotTriggerForItsOwnEntry() {
        harness.setHand(player1, List.of(new MarketwatchPhantom()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent phantom = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, phantom, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A creature entering with power increased above 2 does not trigger")
    void doesNotTriggerWhenStaticBoostRaisesEnteringPower() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new MarketwatchPhantom());
        Permanent solvedCase = harness.addToBattlefieldAndReturn(player1, new CaseOfTheGatewayExpress());
        solvedCase.setSolved(true);
        harness.setHand(player1, List.of(new MarketwatchPhantom()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, phantom, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A disguised creature with printed power above 2 triggers as a 2/2")
    void triggersForFaceDownCreature() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new MarketwatchPhantom());
        harness.setHand(player1, List.of(new GraniteWitness()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, phantom, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Increasing the entering creature's power after entry does not stop the trigger")
    void powerIsNotRecheckedAtResolution() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new MarketwatchPhantom());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new MarketwatchPhantom());
        assertThat(gd.stack).hasSize(1);
        entering.setPowerModifier(2);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, phantom, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, entering, Keyword.FLYING)).isFalse();
    }
}
