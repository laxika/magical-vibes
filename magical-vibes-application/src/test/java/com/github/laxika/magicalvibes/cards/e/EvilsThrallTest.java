package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheMastersOfEvil;
import com.github.laxika.magicalvibes.cards.u.UltronDrone;
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

@CardUsed({EvilsThrall.class, GrizzlyBears.class, TheMastersOfEvil.class, UltronDrone.class})
class EvilsThrallTest extends BaseCardTest {

    private void castEvilsThrall(Permanent target) {
        harness.setHand(player1, List.of(new EvilsThrall()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Steals, untaps, and grants haste through the end of turn without a qualifying Villain")
    void resolvesUntilEndOfTurnWithoutQualifyingVillain() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        castEvilsThrall(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A qualifying Villain extends control through the end of the controller's next turn")
    void qualifyingVillainExtendsControl() {
        harness.addToBattlefield(player1, new TheMastersOfEvil());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        castEvilsThrall(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("An equal mana value Villain does not extend control")
    void equalManaValueDoesNotExtendControl() {
        harness.addToBattlefield(player1, new UltronDrone());
        Permanent target = addCreatureReady(player2, new UltronDrone());

        castEvilsThrall(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("A lower mana value Villain does not extend control")
    void lowerManaValueDoesNotExtendControl() {
        harness.addToBattlefield(player1, new UltronDrone());
        Permanent target = addCreatureReady(player2, new TheMastersOfEvil());

        castEvilsThrall(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("An opponent's greater mana value Villain does not extend control")
    void opponentsVillainDoesNotExtendControl() {
        harness.addToBattlefield(player2, new TheMastersOfEvil());
        Permanent target = addCreatureReady(player2, new UltronDrone());

        castEvilsThrall(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Extended control persists after the qualifying Villain leaves, but haste expires this turn")
    void extendedControlDoesNotRequireVillainToRemain() {
        Permanent villain = harness.addToBattlefieldAndReturn(player1, new TheMastersOfEvil());
        Permanent target = addCreatureReady(player2, new UltronDrone());

        castEvilsThrall(target);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, villain));

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("The Villain condition is checked on resolution, not when the spell is cast")
    void villainLeavingBeforeResolutionDoesNotExtendControl() {
        Permanent villain = harness.addToBattlefieldAndReturn(player1, new TheMastersOfEvil());
        Permanent target = addCreatureReady(player2, new UltronDrone());
        target.tap();
        harness.setHand(player1, List.of(new EvilsThrall()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, villain));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }
}
