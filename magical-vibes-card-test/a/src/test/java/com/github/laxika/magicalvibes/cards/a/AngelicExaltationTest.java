package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicExaltation.class, GrizzlyBears.class})
class AngelicExaltationTest extends BaseCardTest {

    @Test
    @DisplayName("A lone attacker gets +X/+X where X is the number of creatures you control")
    void loneAttackerGetsBoostBasedOnControlledCreatures() {
        harness.addToBattlefield(player1, new AngelicExaltation());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(2));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(5);
    }

    @Test
    @DisplayName("Angelic Exaltation does not trigger when more than one creature attacks")
    void noTriggerWhenNotAlone() {
        harness.addToBattlefield(player1, new AngelicExaltation());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Angelic Exaltation"));
        assertThat(gqs.getEffectivePower(gd, firstAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondAttacker)).isEqualTo(2);
    }

    @Test
    void countsOnlyYourCreaturesIncludingTheAttacker() {
        harness.addToBattlefield(player1, new AngelicExaltation());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @CardUsed(Unsummon.class)
    void countsCreaturesAtResolutionAfterAnotherCreatureLeaves() {
        harness.addToBattlefield(player1, new AngelicExaltation());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(1));
        harness.castAndResolveInstant(player1, 0, other.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    void creaturesEnteringBeforeResolutionIncreaseTheBoostButLaterOnesDoNot() {
        harness.addToBattlefield(player1, new AngelicExaltation());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        harness.addToBattlefield(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);

        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    void multipleExaltationsEachBoostTheLoneAttacker() {
        harness.addToBattlefield(player1, new AngelicExaltation());
        harness.addToBattlefield(player1, new AngelicExaltation());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(6);
    }

    @Test
    void opponentsLoneAttackerDoesNotTriggerYourExaltation() {
        harness.addToBattlefield(player1, new AngelicExaltation());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    void boostExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new AngelicExaltation());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }
}
