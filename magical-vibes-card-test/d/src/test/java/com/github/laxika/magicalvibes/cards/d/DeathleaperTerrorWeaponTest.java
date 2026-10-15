package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathleaperTerrorWeapon.class, GrizzlyBears.class})
class DeathleaperTerrorWeaponTest extends BaseCardTest {

    @Test
    void grantsDoubleStrikeToControlledCreaturesThatEnteredThisTurn() {
        Permanent deathleaper = harness.enterBattlefieldAndReturn(player1, new DeathleaperTerrorWeapon());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, deathleaper, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void doesNotGrantDoubleStrikeToCreaturesThatDidNotEnterOrThatAnOpponentControls() {
        harness.addToBattlefield(player1, new DeathleaperTerrorWeapon());
        Permanent oldCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, oldCreature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void grantsDoubleStrikeToACreatureThatEnteredBeforeDeathleaper() {
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new DeathleaperTerrorWeapon());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void doubleStrikeStopsWhenDeathleaperLeavesTheBattlefield() {
        Permanent deathleaper = harness.enterBattlefieldAndReturn(player1, new DeathleaperTerrorWeapon());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(deathleaper);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void doubleStrikeStopsOnTheNextTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent deathleaper = harness.enterBattlefieldAndReturn(player1, new DeathleaperTerrorWeapon());
        assertThat(gqs.hasKeyword(gd, deathleaper, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, deathleaper, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void enteringUnderAnotherControllerStillQualifiesAfterChangingControl() {
        harness.addToBattlefield(player1, new DeathleaperTerrorWeapon());
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void canBeCastDuringAnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castFromHand(player1, new DeathleaperTerrorWeapon(), "{2}{R}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Deathleaper, Terror Weapon");
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Deathleaper, Terror Weapon"),
                Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void canAttackTheTurnItEnters() {
        Permanent deathleaper = harness.enterBattlefieldAndReturn(player1, new DeathleaperTerrorWeapon());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThat(deathleaper.isAttacking()).isTrue();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat(player1);
        harness.assertLife(player2, 14);
    }
}
