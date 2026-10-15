package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.e.EchoingRuin;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlobadGoblinTinkerer.class, AngelsFeather.class, CrazedGoblin.class, MycosynthLattice.class, EchoingRuin.class})
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

    @Test
    @DisplayName("Can activate while summoning sick and tapped without mana")
    void canActivateWhileSummoningSickAndTapped() {
        Permanent slobad = harness.addToBattlefieldAndReturn(player1, new SlobadGoblinTinkerer());
        slobad.setSummoningSick(true);
        slobad.tap();
        harness.addToBattlefield(player1, new AngelsFeather());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Angel's Feather");
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(slobad.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May sacrifice the targeted artifact, leaving no legal target at resolution")
    void canSacrificeTargetedArtifact() {
        addCreatureReady(player1, new SlobadGoblinTinkerer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Angel's Feather");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angel's Feather");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Granted indestructible prevents destruction of a noncreature artifact")
    void protectedArtifactSurvivesDestruction() {
        addCreatureReady(player1, new SlobadGoblinTinkerer());
        harness.addToBattlefield(player1, new AngelsFeather());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new EchoingRuin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Angel's Feather");
    }
}
