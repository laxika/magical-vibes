package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeroInTraining;
import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.cards.v.VeteransArmaments;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainAmericaUnbowed.class, HeroInTraining.class, GrizzlyBears.class,
        MetathranSoldier.class, VeteransArmaments.class})
class CaptainAmericaUnbowedTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, your Soldiers and Heroes gain indestructible until end of turn")
    void entersAndProtectsSoldiersAndHeroes() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroInTraining());
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new MetathranSoldier());
        Permanent nonMatching = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingSoldier = harness.addToBattlefieldAndReturn(player2, new MetathranSoldier());

        castCaptain();
        resolveAllTriggers();

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
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void castCaptain() {
        harness.castFromHand(player1, new CaptainAmericaUnbowed(), "{3}{W}");
    }

    @Test
    @CardUsed({CaptainAmericaUnbowed.class, VeteransArmaments.class})
    @DisplayName("Noncreature Soldiers also gain indestructible")
    void protectsNoncreatureSoldiers() {
        castCaptain();
        harness.passBothPriorities();
        Permanent armaments = harness.addToBattlefieldAndReturn(player1, new VeteransArmaments());
        Permanent opposingArmaments = harness.addToBattlefieldAndReturn(player2, new VeteransArmaments());

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, armaments, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingArmaments, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The trigger selects Heroes when it resolves and does not protect later arrivals")
    void selectsHeroesAtResolution() {
        castCaptain();
        harness.passBothPriorities();
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new HeroInTraining());
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.INDESTRUCTIBLE)).isFalse();

        resolveAllTriggers();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new HeroInTraining());

        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        castCaptain();
        resolveAllTriggers();

        Permanent captain = findPermanent(player1, "Captain America, Unbowed");
        assertThat(gqs.hasKeyword(gd, captain, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
