package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BallynockCohort;
import com.github.laxika.magicalvibes.cards.b.BoggartRamGang;
import com.github.laxika.magicalvibes.cards.b.BoonReflection;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldenglowMoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LastBreath.class, BallynockCohort.class, BoggartRamGang.class, Forest.class,
        BoonReflection.class, GoldenglowMoth.class})
class LastBreathTest extends BaseCardTest {

    private void giveLastBreath() {
        harness.setHand(player1, List.of(new LastBreath()));
        harness.addMana(player1, ManaColor.WHITE, 2);
    }

    @Test
    @DisplayName("Exiles a power-2 creature and its controller gains 4 life")
    void exilesCreatureAndControllerGainsLife() {
        Permanent target = addCreatureReady(player2, new BallynockCohort());
        harness.setLife(player2, 20);
        giveLastBreath();

        harness.castAndResolveInstant(player1, 0, target.getId());

        // Target removed from battlefield and moved to exile (not graveyard)
        harness.assertNotOnBattlefield(player2, "Ballynock Cohort");
        harness.assertNotInGraveyard(player2, "Ballynock Cohort");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Ballynock Cohort"));

        // The exiled creature's controller (player2) gains the life, not the caster
        harness.assertLife(player2, 24);
    }

    @Test
    @DisplayName("Life goes to the controller of the exiled creature (caster's own creature)")
    void lifeGoesToCasterWhenTargetingOwnCreature() {
        Permanent target = addCreatureReady(player1, new BallynockCohort());
        harness.setLife(player1, 20);
        giveLastBreath();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Ballynock Cohort");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Exiles a creature with power less than 2")
    void exilesCreatureWithPowerLessThanTwo() {
        Permanent target = addCreatureReady(player2, new GoldenglowMoth());
        harness.setLife(player2, 20);
        giveLastBreath();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Goldenglow Moth");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Goldenglow Moth"));
        harness.assertLife(player2, 24);
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetHighPowerCreature() {
        // Provide a legal target so the spell is castable at all
        addCreatureReady(player2, new BallynockCohort());
        Permanent bigGuy = addCreatureReady(player2, new BoggartRamGang());
        giveLastBreath();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bigGuy.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2 or less");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player2, new BallynockCohort());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        giveLastBreath();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The target controller's life gain is doubled by a replacement effect")
    void targetControllerReceivesLifeGainThroughReplacementEffect() {
        Permanent target = addCreatureReady(player2, new BallynockCohort());
        harness.addToBattlefield(player2, new BoonReflection());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        giveLastBreath();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Ballynock Cohort");
        harness.assertOnBattlefield(player2, "Boon Reflection");
        harness.assertLife(player2, 28);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a creature whose static bonus raises its power above 2")
    void cannotTargetCreatureWithBoostedPower() {
        Permanent target = addCreatureReady(player2, new BallynockCohort());
        addCreatureReady(player2, new GoldenglowMoth());
        giveLastBreath();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2 or less");
    }

    @Test
    @DisplayName("No exile or life gain if the target's power rises above 2 before resolution")
    void targetBecomesIllegalWhenPowerIncreases() {
        Permanent target = addCreatureReady(player2, new BallynockCohort());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        giveLastBreath();

        harness.castInstant(player1, 0, target.getId());
        harness.addToBattlefield(player2, new GoldenglowMoth());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ballynock Cohort");
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Ballynock Cohort"));
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Last Breath");
    }

    @Test
    @DisplayName("Fizzles with no life gain if the target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addCreatureReady(player2, new BallynockCohort());
        harness.setLife(player2, 20);
        giveLastBreath();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Ballynock Cohort"));
    }
}
