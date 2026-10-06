package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FrogButler;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighFlyingAce.class, FrogButler.class, Island.class})
class HighFlyingAceTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants flying to a target creature without flying")
    void grantsFlyingToCreatureWithoutFlying() {
        addCreatureReady(player1, new HighFlyingAce());
        Permanent target = addCreatureReady(player1, new FrogButler());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new HighFlyingAce());
        Permanent target = addCreatureReady(player1, new FrogButler());
        addMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Ability cannot target a creature that already has flying")
    void cannotTargetCreatureWithFlying() {
        addCreatureReady(player1, new HighFlyingAce());
        Permanent target = addCreatureReady(player1, new HighFlyingAce());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without flying");
    }

    @Test
    @DisplayName("Ability can only be activated at sorcery speed")
    void onlyAtSorcerySpeed() {
        addCreatureReady(player1, new HighFlyingAce());
        Permanent target = addCreatureReady(player1, new FrogButler());
        addMana();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can grant flying to an opponent's creature")
    void canTargetOpponentsCreature() {
        addCreatureReady(player1, new HighFlyingAce());
        Permanent target = addCreatureReady(player2, new FrogButler());
        addMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness and being tapped do not prevent activation")
    void canActivateWhileSummoningSickAndTapped() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HighFlyingAce());
        source.setSummoningSick(true);
        source.tap();
        Permanent target = addCreatureReady(player1, new FrogButler());
        addMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ability cannot be activated outside a main phase")
    void cannotActivateDuringCombat() {
        addCreatureReady(player1, new HighFlyingAce());
        Permanent target = addCreatureReady(player1, new FrogButler());
        addMana();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Ability cannot be activated while another ability is on the stack")
    void cannotActivateWithNonemptyStack() {
        addCreatureReady(player1, new HighFlyingAce());
        Permanent target = addCreatureReady(player1, new FrogButler());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ability can be activated during the postcombat main phase")
    void canActivateDuringPostcombatMainPhase() {
        addCreatureReady(player1, new HighFlyingAce());
        Permanent target = addCreatureReady(player1, new FrogButler());
        addMana();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void cannotTargetLand() {
        addCreatureReady(player1, new HighFlyingAce());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot target a creature that gained flying earlier this turn")
    void cannotTargetCreatureWithGrantedFlying() {
        addCreatureReady(player1, new HighFlyingAce());
        Permanent target = addCreatureReady(player1, new FrogButler());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without flying");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
