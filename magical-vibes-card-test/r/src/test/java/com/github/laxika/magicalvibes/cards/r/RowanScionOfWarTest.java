package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.g.GrandBallGuest;
import com.github.laxika.magicalvibes.cards.s.SplashySpellcaster;
import com.github.laxika.magicalvibes.cards.s.StingbladeAssassin;
import com.github.laxika.magicalvibes.cards.w.WallOfBlood;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RowanScionOfWar.class, WallOfBlood.class, LightningStrike.class,
        GrandBallGuest.class, SplashySpellcaster.class, StingbladeAssassin.class})
class RowanScionOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces a matching spell by life lost this turn")
    void reducesMatchingSpellByLifeLostThisTurn() {
        addCreatureReady(player1, new WallOfBlood());
        addCreatureReady(player1, new RowanScionOfWar());
        harness.setLife(player1, 20);
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Snapshots life lost when the ability resolves")
    void snapshotsLifeLostWhenAbilityResolves() {
        addCreatureReady(player1, new WallOfBlood());
        addCreatureReady(player1, new RowanScionOfWar());
        harness.setLife(player1, 20);
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not reduce a matching spell when no life was lost")
    void doesNotReduceWithoutLifeLoss() {
        addCreatureReady(player1, new RowanScionOfWar());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void lifeLostAfterResolutionDoesNotIncreaseReduction() {
        addCreatureReady(player1, new WallOfBlood());
        addCreatureReady(player1, new RowanScionOfWar());
        prepareMainPhase();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrandBallGuest()));
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reducesBlackSpellsAndIgnoresLifeGained() {
        addCreatureReady(player1, new WallOfBlood());
        addCreatureReady(player1, new RowanScionOfWar());
        prepareMainPhase();
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new StingbladeAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reductionAppliesToEveryMatchingSpell() {
        addCreatureReady(player1, new WallOfBlood());
        addCreatureReady(player1, new RowanScionOfWar());
        prepareMainPhase();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrandBallGuest(), new GrandBallGuest()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Grand Ball Guest")).isEqualTo(2);
    }

    @Test
    void doesNotReduceBlueSpells() {
        addCreatureReady(player1, new WallOfBlood());
        addCreatureReady(player1, new RowanScionOfWar());
        prepareMainPhase();
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SplashySpellcaster()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducesSpellsThatAreBothBlackAndRed() {
        addCreatureReady(player1, new WallOfBlood());
        addCreatureReady(player1, new RowanScionOfWar());
        prepareMainPhase();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new RowanScionOfWar()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void abilityStillResolvesAfterRowanDies() {
        addCreatureReady(player1, new WallOfBlood());
        var rowan = addCreatureReady(player1, new RowanScionOfWar());
        prepareMainPhase();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, rowan.getId());
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Rowan, Scion of War");

        harness.setHand(player1, List.of(new GrandBallGuest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reductionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new WallOfBlood());
        addCreatureReady(player1, new RowanScionOfWar());
        prepareMainPhase();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new StingbladeAssassin()));
        harness.setLibrary(player2, List.of(new GrandBallGuest(), new GrandBallGuest()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            harness.ensurePriority(player1);
        }
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceColoredMana() {
        addCreatureReady(player1, new WallOfBlood());
        addCreatureReady(player1, new RowanScionOfWar());
        prepareMainPhase();
        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrandBallGuest()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceOpponentsSpells() {
        addCreatureReady(player1, new WallOfBlood());
        addCreatureReady(player1, new RowanScionOfWar());
        prepareMainPhase();
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new StingbladeAssassin()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.passPriority(player1);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        addCreatureReady(player1, new RowanScionOfWar());
        prepareMainPhase();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateDuringOpponentsTurn() {
        addCreatureReady(player1, new RowanScionOfWar());
        prepareMainPhase();
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithNonemptyStack() {
        addCreatureReady(player1, new WallOfBlood());
        addCreatureReady(player1, new RowanScionOfWar());
        prepareMainPhase();
        harness.activateAbility(player1, 0, null, null);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void activationTapsRowanAndPreventsAnotherActivation() {
        var rowan = addCreatureReady(player1, new RowanScionOfWar());
        prepareMainPhase();
        harness.activateAbility(player1, 0, null, null);
        assertThat(rowan.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new RowanScionOfWar());
        prepareMainPhase();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
