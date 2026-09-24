package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GoblinHero;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
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

@CardUsed({CaptainAmericaUnbowed.class, GoblinHero.class, GrizzlyBears.class, MetathranSoldier.class})
class CaptainAmericaUnbowedTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, your Soldiers and Heroes gain indestructible until end of turn")
    void entersAndProtectsSoldiersAndHeroes() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new GoblinHero());
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new MetathranSoldier());
        Permanent nonMatching = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingSoldier = harness.addToBattlefieldAndReturn(player2, new MetathranSoldier());

        castCaptain();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent captain = findPermanent(player1, "Captain America, Unbowed");
        assertThat(gqs.hasKeyword(gd, captain, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, hero, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonMatching, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSoldier, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The granted indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new MetathranSoldier());

        castCaptain();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void castCaptain() {
        harness.setHand(player1, List.of(new CaptainAmericaUnbowed()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
