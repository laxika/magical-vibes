package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CanopyBaloth;
import com.github.laxika.magicalvibes.cards.m.MurasaBrute;
import com.github.laxika.magicalvibes.cards.s.SmiteTheMonstrous;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KarganWarleader.class, MurasaBrute.class, CanopyBaloth.class, SmiteTheMonstrous.class})
class KarganWarleaderTest extends BaseCardTest {

    @Test
    void buffsOtherWarriorsYouControl() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new MurasaBrute());
        int basePower = gqs.getEffectivePower(gd, warrior);
        int baseToughness = gqs.getEffectiveToughness(gd, warrior);

        harness.addToBattlefield(player1, new KarganWarleader());

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(baseToughness + 1);
    }

    @Test
    void doesNotBuffItselfNonWarriorsOrOpposingWarriors() {
        Permanent nonWarrior = harness.addToBattlefieldAndReturn(player1, new CanopyBaloth());
        int nonWarriorPower = gqs.getEffectivePower(gd, nonWarrior);
        int nonWarriorToughness = gqs.getEffectiveToughness(gd, nonWarrior);

        Permanent opposingWarrior = harness.addToBattlefieldAndReturn(player2, new MurasaBrute());
        int opposingWarriorPower = gqs.getEffectivePower(gd, opposingWarrior);
        int opposingWarriorToughness = gqs.getEffectiveToughness(gd, opposingWarrior);

        KarganWarleader card = new KarganWarleader();
        card.setPower(10);
        card.setToughness(10);
        Permanent warleader = harness.addToBattlefieldAndReturn(player1, card);

        assertThat(gqs.getEffectivePower(gd, warleader)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, warleader)).isEqualTo(10);
        assertThat(gqs.getEffectivePower(gd, nonWarrior)).isEqualTo(nonWarriorPower);
        assertThat(gqs.getEffectiveToughness(gd, nonWarrior)).isEqualTo(nonWarriorToughness);
        assertThat(gqs.getEffectivePower(gd, opposingWarrior)).isEqualTo(opposingWarriorPower);
        assertThat(gqs.getEffectiveToughness(gd, opposingWarrior)).isEqualTo(opposingWarriorToughness);
    }

    @Test
    void multipleWarleadersStackTheirBonuses() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new MurasaBrute());
        int basePower = gqs.getEffectivePower(gd, warrior);
        int baseToughness = gqs.getEffectiveToughness(gd, warrior);

        harness.addToBattlefield(player1, new KarganWarleader());
        harness.addToBattlefield(player1, new KarganWarleader());

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(baseToughness + 2);
    }

    @Test
    void buffsWarriorsEnteringAfterTheWarleader() {
        Permanent opposingWarrior = harness.addToBattlefieldAndReturn(player2, new MurasaBrute());
        int basePower = gqs.getEffectivePower(gd, opposingWarrior);
        int baseToughness = gqs.getEffectiveToughness(gd, opposingWarrior);
        harness.addToBattlefield(player1, new KarganWarleader());

        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new MurasaBrute());

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(baseToughness + 1);
    }

    @Test
    void warleadersBuffEachOtherAndBonusesEndWhenOneIsDestroyed() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new MurasaBrute());
        int warriorPower = gqs.getEffectivePower(gd, warrior);
        int warriorToughness = gqs.getEffectiveToughness(gd, warrior);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KarganWarleader());
        int warleaderPower = gqs.getEffectivePower(gd, first);
        int warleaderToughness = gqs.getEffectiveToughness(gd, first);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new KarganWarleader());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(warleaderPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(warleaderToughness + 1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(warleaderPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(warleaderToughness + 1);
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(warriorPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(warriorToughness + 2);

        harness.setHand(player1, List.of(new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, first.getId());

        harness.assertInGraveyard(player1, "Kargan Warleader");
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(warleaderPower);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(warleaderToughness);
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(warriorPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(warriorToughness + 1);
    }
}
