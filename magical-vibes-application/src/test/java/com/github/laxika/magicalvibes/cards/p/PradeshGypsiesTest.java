package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PradeshGypsies.class, GrizzlyBears.class, FountainOfYouth.class, Unsummon.class})
class PradeshGypsiesTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets -2/-0 until end of turn when the ability resolves")
    void weakensTargetCreature() {
        setupGypsies();
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isEqualTo(-2);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Taps the Gypsies when activated")
    void tapsOnActivation() {
        setupGypsies();
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(findPermanent(player1, "Pradesh Gypsies").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void weakensOpponentsCreature() {
        setupGypsies();
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(-2);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        setupGypsies();
        Permanent gypsies = findPermanent(player1, "Pradesh Gypsies");

        harness.activateAbility(player1, 0, null, gypsies.getId());
        harness.passBothPriorities();

        assertThat(gypsies.getPowerModifier()).isEqualTo(-2);
        assertThat(gypsies.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        setupGypsies();
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Pays the full activation cost")
    void paysActivationCost() {
        setupGypsies();
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        setupGypsies();
        gd.playerManaPools.get(player1.getId()).clear();
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenAlreadyTapped() {
        setupGypsies();
        findPermanent(player1, "Pradesh Gypsies").tap();
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate while it has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent gypsies = harness.addToBattlefieldAndReturn(player1, new PradeshGypsies());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gypsies.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Weakening wears off at cleanup")
    void weakeningWearsOff() {
        setupGypsies();
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Two activations stack and can reduce power below zero without changing toughness")
    void multipleActivationsAreCumulative() {
        setupGypsies();
        addCreatureReady(player1, new PradeshGypsies());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent bear = findPermanent(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.activateAbility(player1, 2, null, bear.getId());
        resolveAllTriggers();

        assertThat(bear.getPowerModifier()).isEqualTo(-4);
        assertThat(bear.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ability resolves even after its source returns to hand")
    void resolvesAfterSourceLeavesBattlefield() {
        setupGypsies();
        Permanent bear = findPermanent(player1, "Grizzly Bears");
        UUID sourceId = harness.getPermanentId(player1, "Pradesh Gypsies");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.castInstant(player2, 0, sourceId);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pradesh Gypsies");
        harness.assertNotOnBattlefield(player1, "Pradesh Gypsies");
        assertThat(bear.getPowerModifier()).isZero();

        resolveAllTriggers();

        assertThat(bear.getPowerModifier()).isEqualTo(-2);
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Ability has no effect when its target leaves before resolution")
    void doesNotAffectRemovedTarget() {
        setupGypsies();
        Permanent bear = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void setupGypsies() {
        addCreatureReady(player1, new PradeshGypsies());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
