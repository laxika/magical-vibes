package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DwynensElite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarduHateblade;
import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RushOfBattle.class, DwynensElite.class, GrizzlyBears.class,
        MarduHateblade.class, WetlandSambar.class})
class RushOfBattleTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures and grants lifelink to your Warriors")
    void boostsCreaturesAndGrantsLifelinkToWarriors() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new DwynensElite());
        Permanent nonWarrior = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentWarrior = harness.addToBattlefieldAndReturn(player2, new DwynensElite());

        castRushOfBattle();

        assertThat(warrior.getEffectivePower()).isEqualTo(4);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(3);
        assertThat(nonWarrior.getEffectivePower()).isEqualTo(4);
        assertThat(nonWarrior.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponentWarrior.getEffectivePower()).isEqualTo(2);
        assertThat(opponentWarrior.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonWarrior, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentWarrior, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The boost and lifelink wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new DwynensElite());
        castRushOfBattle();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warrior.getEffectivePower()).isEqualTo(2);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Only the Warrior's combat damage gains life")
    void warriorCombatDamageGainsLife() {
        addCreatureReady(player1, new MarduHateblade());
        addCreatureReady(player1, new WetlandSambar());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        castRushOfBattle();
        declareAttackers(List.of(0, 1));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Creatures entering after resolution receive neither effect")
    void creaturesEnteringAfterResolutionAreUnaffected() {
        castRushOfBattle();

        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new MarduHateblade());
        Permanent nonWarrior = harness.addToBattlefieldAndReturn(player1, new WetlandSambar());

        assertThat(warrior.getEffectivePower()).isEqualTo(1);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(1);
        assertThat(nonWarrior.getEffectivePower()).isEqualTo(2);
        assertThat(nonWarrior.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonWarrior, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Creatures present at resolution are affected even if absent when cast")
    void creaturesEnteringBeforeResolutionAreAffected() {
        harness.castFromHand(player1, new RushOfBattle(), "{3}{W}");
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new MarduHateblade());

        harness.passBothPriorities();

        assertThat(warrior.getEffectivePower()).isEqualTo(3);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Repeated casts stack the boost but do not multiply lifelink")
    void repeatedCastsStackBoostButNotLifelink() {
        Permanent warrior = addCreatureReady(player1, new MarduHateblade());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        castRushOfBattle();
        castRushOfBattle();

        assertThat(warrior.getEffectivePower()).isEqualTo(5);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(3);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    private void castRushOfBattle() {
        harness.castFromHand(player1, new RushOfBattle(), "{3}{W}");
        harness.passBothPriorities();
    }
}
