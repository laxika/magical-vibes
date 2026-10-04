package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaunterOfNightveil.class, GrizzlyBears.class})
class HaunterOfNightveilTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent creatures get -1/-0")
    void debuffsOpponentCreatures() {
        harness.addToBattlefield(player1, new HaunterOfNightveil());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Own creatures are unaffected")
    void doesNotAffectOwnCreatures() {
        harness.addToBattlefield(player1, new HaunterOfNightveil());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not debuff itself")
    void doesNotDebuffItself() {
        harness.addToBattlefield(player1, new HaunterOfNightveil());

        Permanent haunter = findPermanent(player1, "Haunter of Nightveil");

        assertThat(gqs.getEffectivePower(gd, haunter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, haunter)).isEqualTo(4);
    }

    @Test
    @DisplayName("Two copies stack to -2/-0")
    void twoCopiesStack() {
        harness.addToBattlefield(player1, new HaunterOfNightveil());
        harness.addToBattlefield(player1, new HaunterOfNightveil());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Penalty applies on resolve and is removed when it leaves")
    void penaltyAppliesOnResolveAndEndsWhenItLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.castFromHand(player1, new HaunterOfNightveil(), "{3}{U}{B}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Haunter of Nightveil"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opposing Haunters each reduce only the other player's creature")
    void opposingHauntersDebuffEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HaunterOfNightveil());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HaunterOfNightveil());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("Stacked penalties apply to newly entering creatures and allow negative power")
    void stackedPenaltiesCanReduceNewCreaturePowerBelowZero() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new HaunterOfNightveil());
        }
        Permanent opponent = harness.enterBattlefieldAndReturn(player2, new HaunterOfNightveil());

        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(4);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player2, "Haunter of Nightveil");
    }
}
