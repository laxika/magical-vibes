package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlabasterHostSanctifier;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CutShort.class, JaceBeleren.class, AlabasterHostSanctifier.class})
class CutShortTest extends BaseCardTest {

    @Test
    void destroysTappedCreature() {
        Permanent bears = addCreatureReady(player2, new AlabasterHostSanctifier());
        bears.tap();

        castCutShort(player1, bears.getId());

        harness.assertNotOnBattlefield(player2, "Alabaster Host Sanctifier");
        harness.assertInGraveyard(player2, "Alabaster Host Sanctifier");
    }

    @Test
    void destroysPlaneswalkerActivatedThisTurn() {
        Permanent jace = addReadyJace(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new CutShort()));
        addCutShortMana(player1);

        harness.castAndResolveInstant(player1, 0, jace.getId());

        harness.assertNotOnBattlefield(player1, "Jace Beleren");
        harness.assertInGraveyard(player1, "Jace Beleren");
    }

    @Test
    void cannotTargetUnactivatedPlaneswalkerOrUntappedCreature() {
        Permanent jace = addReadyJace(player2);
        harness.setHand(player1, List.of(new CutShort()));
        addCutShortMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, jace.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent bears = addCreatureReady(player2, new AlabasterHostSanctifier());
        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotDestroyCreatureThatUntapsBeforeResolution() {
        Permanent bears = addCreatureReady(player2, new AlabasterHostSanctifier());
        bears.tap();
        harness.setHand(player1, List.of(new CutShort()));
        addCutShortMana(player1);

        harness.castInstant(player1, 0, bears.getId());
        bears.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Alabaster Host Sanctifier");
        harness.assertInGraveyard(player1, "Cut Short");
    }

    @Test
    void canDestroyPlaneswalkerBeforeItsActivatedAbilityResolves() {
        Permanent jace = addReadyJace(player1);
        harness.setHand(player1, List.of(new CutShort()));
        addCutShortMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castAndResolveInstant(player1, 0, jace.getId());

        harness.assertNotOnBattlefield(player1, "Jace Beleren");
        harness.assertInGraveyard(player1, "Jace Beleren");
        harness.passBothPriorities();
    }

    @Test
    void convokePaysEntireCostUsingSummoningSickCreatures() {
        Permanent target = addCreatureReady(player2, new AlabasterHostSanctifier());
        target.tap();
        Permanent white = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CutShort()));

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                List.of(white.getId(), first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Alabaster Host Sanctifier");
        assertThat(white.isTapped()).isTrue();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    void cannotTargetUntappedCreatureEvenWhenItWouldConvoke() {
        Permanent creature = addCreatureReady(player1, new AlabasterHostSanctifier());
        harness.setHand(player1, List.of(new CutShort()));
        addCutShortMana(player1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(creature.getId()), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCutShort(Player player, java.util.UUID targetId) {
        harness.setHand(player, List.of(new CutShort()));
        addCutShortMana(player);
        harness.castAndResolveInstant(player, 0, targetId);
    }

    private void addCutShortMana(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private Permanent addReadyJace(Player player) {
        Permanent jace = harness.addToBattlefieldAndReturn(player, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        jace.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return jace;
    }
}
