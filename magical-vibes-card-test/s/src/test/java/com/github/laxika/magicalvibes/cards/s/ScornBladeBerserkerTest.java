package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlabasterHostSanctifier;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({ScornBladeBerserker.class, AlabasterHostSanctifier.class, Island.class})
class ScornBladeBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts a counter on another creature and grants the sacrifice draw ability")
    void backsUpAnotherCreature() {
        Permanent sanctifier = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        castScornBladeBerserker();
        resolveEtbTargeting(sanctifier);
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int sanctifierIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sanctifier);
        harness.activateAbility(player1, sanctifierIndex, null, null);
        harness.passBothPriorities();

        assertThat(sanctifier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Alabaster Host Sanctifier");
    }

    @Test
    @DisplayName("Backup targeting this creature only puts on the counter")
    void backsUpItself() {
        Permanent berserker = castScornBladeBerserker();
        resolveEtbTargeting(berserker);

        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The granted sacrifice draw ability expires at the end of the turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        Permanent sanctifier = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        castScornBladeBerserker();
        resolveEtbTargeting(sanctifier);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        int sanctifierIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sanctifier);
        assertThatThrownBy(() -> harness.activateAbility(player1, sanctifierIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("The Berserker can sacrifice itself while summoning sick and draws only on resolution")
    void sacrificesItselfToDraw() {
        Permanent berserker = castScornBladeBerserker();
        resolveEtbTargeting(berserker);
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(berserker);
        harness.activateAbility(player1, index, null, null);

        harness.assertNotOnBattlefield(player1, "Scorn-Blade Berserker");
        harness.assertInGraveyard(player1, "Scorn-Blade Berserker");
        harness.assertNotInHand(player1, "Island");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Backup can target an opponent's creature and its controller draws the card")
    void backsUpOpponentsCreature() {
        Permanent sanctifier = harness.addToBattlefieldAndReturn(player2, new AlabasterHostSanctifier());
        castScornBladeBerserker();
        resolveEtbTargeting(sanctifier);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Island()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThat(sanctifier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(sanctifier);
        harness.activateAbility(player2, index, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Alabaster Host Sanctifier");
        harness.assertInHand(player2, "Island");
        harness.assertNotInHand(player1, "Island");
        harness.assertOnBattlefield(player1, "Scorn-Blade Berserker");
    }

    @Test
    @DisplayName("The granted ability remains usable after the Berserker is sacrificed")
    void grantedAbilitySurvivesSourceLeaving() {
        Permanent sanctifier = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        Permanent berserker = castScornBladeBerserker();
        resolveEtbTargeting(sanctifier);
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(berserker);
        harness.activateAbility(player1, sourceIndex, null, null);
        harness.passBothPriorities();
        int targetIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sanctifier);
        harness.activateAbility(player1, targetIndex, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scorn-Blade Berserker");
        harness.assertInGraveyard(player1, "Alabaster Host Sanctifier");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private Permanent castScornBladeBerserker() {
        harness.setHand(player1, List.of(new ScornBladeBerserker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Scorn-Blade Berserker");
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
