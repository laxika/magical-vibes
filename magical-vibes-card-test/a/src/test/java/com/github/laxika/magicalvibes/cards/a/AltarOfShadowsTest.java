package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AltarOfShadows.class, AlphaMyr.class, Shatter.class})
class AltarOfShadowsTest extends BaseCardTest {

    @Test
    @DisplayName("First main phase adds black mana for each charge counter")
    void firstMainPhaseAddsBlackManaForChargeCounters() {
        Permanent altar = addAltar(player1);
        altar.setCounterCount(CounterType.CHARGE, 3);

        advanceToFirstMainPhase(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    @Test
    @DisplayName("First main phase trigger only adds mana on the controller's turn")
    void firstMainPhaseTriggerOnlyAddsManaOnControllersTurn() {
        Permanent altar = addAltar(player1);
        altar.setCounterCount(CounterType.CHARGE, 3);

        advanceToFirstMainPhase(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Activated ability destroys a creature and adds a charge counter")
    void activatedAbilityDestroysCreatureAndAddsChargeCounter() {
        Permanent altar = addAltar(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alpha Myr");
        harness.assertInGraveyard(player2, "Alpha Myr");
        assertThat(altar.isTapped()).isTrue();
        assertThat(altar.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability requires seven mana")
    void activatedAbilityRequiresSevenMana() {
        addAltar(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability cannot be used while Altar of Shadows is tapped")
    void activatedAbilityCannotBeUsedWhileTapped() {
        Permanent altar = addAltar(player1);
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, firstTarget.getId());
        harness.passBothPriorities();

        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, secondTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability cannot target a noncreature permanent")
    void activatedAbilityCannotTargetNoncreaturePermanent() {
        addAltar(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AltarOfShadows());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void firstMainPhaseWithoutChargeCountersAddsNoMana() {
        addAltar(player1);

        advanceToFirstMainPhase(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void counterAddedInResponseIsCountedWhenMainPhaseTriggerResolves() {
        Permanent altar = addAltar(player1);
        altar.setCounterCount(CounterType.CHARGE, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());

        advanceToFirstMainPhase(player1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(altar.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    @Test
    void illegalTargetPreventsAddingChargeCounter() {
        Permanent altar = addAltar(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, 0, null, target.getId());

        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Alpha Myr");
        assertThat(altar.isTapped()).isTrue();
        assertThat(altar.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void mainPhaseTriggerUsesLastKnownCountersWhenAltarIsDestroyed() {
        Permanent altar = addAltar(player1);
        altar.setCounterCount(CounterType.CHARGE, 3);
        advanceToFirstMainPhase(player1);

        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, altar.getId());
        harness.assertInGraveyard(player1, "Altar of Shadows");
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    @Test
    void destroyingAltarInResponseDoesNotPreventCreatureDestruction() {
        Permanent altar = addAltar(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, 0, null, target.getId());

        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, altar.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Altar of Shadows");
        harness.assertInGraveyard(player2, "Alpha Myr");
        assertThat(altar.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void secondMainPhaseDoesNotAddMana() {
        Permanent altar = addAltar(player1);
        altar.setCounterCount(CounterType.CHARGE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    private Permanent addAltar(Player owner) {
        return harness.addToBattlefieldAndReturn(owner, new AltarOfShadows());
    }

    private void advanceToFirstMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.PRECOMBAT_MAIN);
    }
}
