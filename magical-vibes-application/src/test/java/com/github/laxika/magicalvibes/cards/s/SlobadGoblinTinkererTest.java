package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlobadGoblinTinkerer.class, AngelsFeather.class, CrazedGoblin.class, MycosynthLattice.class})
class SlobadGoblinTinkererTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact grants target artifact indestructible")
    void sacrificeArtifactGrantsIndestructible() {
        addCreatureReady(player1, new SlobadGoblinTinkerer());
        harness.addToBattlefield(player1, new AngelsFeather());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Angel's Feather");
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Granted indestructible wears off at end of turn")
    void indestructibleWearsOff() {
        addCreatureReady(player1, new SlobadGoblinTinkerer());
        harness.addToBattlefield(player1, new AngelsFeather());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        addCreatureReady(player1, new SlobadGoblinTinkerer());
        harness.addToBattlefield(player1, new AngelsFeather());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrazedGoblin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without an artifact to sacrifice")
    void cannotActivateWithoutArtifactToSacrifice() {
        addCreatureReady(player1, new SlobadGoblinTinkerer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can sacrifice itself when it is an artifact")
    void canSacrificeItselfWhenItIsAnArtifact() {
        addCreatureReady(player1, new SlobadGoblinTinkerer());
        harness.addToBattlefield(player2, new MycosynthLattice());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Slobad, Goblin Tinkerer");
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
