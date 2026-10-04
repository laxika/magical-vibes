package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EllieVengefulHunter.class, GrizzlyBears.class})
class EllieVengefulHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature deals damage and grants indestructible")
    void sacrificesAnotherCreatureDealsDamageAndGrantsIndestructible() {
        Permanent ellie = addCreatureReady(player1, new EllieVengefulHunter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gqs.hasKeyword(gd, ellie, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a permanent with the ability")
    void cannotTargetPermanent() {
        addCreatureReady(player1, new EllieVengefulHunter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent ellie = addCreatureReady(player1, new EllieVengefulHunter());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, ellie, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ellie, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without another creature to sacrifice")
    void cannotActivateWithoutAnotherCreature() {
        addCreatureReady(player1, new EllieVengefulHunter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Life and sacrifice are paid before damage and indestructible resolve")
    void costsArePaidBeforeResolution() {
        Permanent ellie = addCreatureReady(player1, new EllieVengefulHunter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, ellie, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gqs.hasKeyword(gd, ellie, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Ellie can target her controller")
    void canTargetController() {
        Permanent ellie = addCreatureReady(player1, new EllieVengefulHunter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        assertThat(gqs.hasKeyword(gd, ellie, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Insufficient life prevents activation without sacrificing a creature")
    void cannotPayWithOnlyOneLife() {
        addCreatureReady(player1, new EllieVengefulHunter());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ellie can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent ellie = harness.addToBattlefieldAndReturn(player1, new EllieVengefulHunter());
        ellie.setSummoningSick(true);
        ellie.setTapped(true);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(gqs.hasKeyword(gd, ellie, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
