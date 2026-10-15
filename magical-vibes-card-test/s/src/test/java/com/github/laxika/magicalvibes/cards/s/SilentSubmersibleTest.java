package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceWielderOfMysteries;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilentSubmersible.class, GrizzlyBears.class, JaceWielderOfMysteries.class})
class SilentSubmersibleTest extends BaseCardTest {

    @Test
    @DisplayName("Is not a creature before being crewed")
    void notACreatureBeforeCrew() {
        Permanent submersible = addSubmersibleReady(player1);

        assertThat(gqs.isCreature(gd, submersible)).isFalse();
    }

    @Test
    @DisplayName("Crew animates the Vehicle and taps the creature used to crew it")
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent submersible = addSubmersibleReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, submersible)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Draws a card when it deals combat damage to a player")
    void drawsOnCombatDamageToPlayer() {
        Permanent submersible = addSubmersibleReady(player1);
        submersible.setAnimatedUntilEndOfTurn(true);
        submersible.setAnimatedPower(2);
        submersible.setAnimatedToughness(3);
        submersible.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Does not draw a card when blocked")
    void noDrawWhenBlocked() {
        Permanent submersible = addSubmersibleReady(player1);
        submersible.setAnimatedUntilEndOfTurn(true);
        submersible.setAnimatedPower(2);
        submersible.setAnimatedToughness(3);
        submersible.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Draws a card when it deals combat damage to a planeswalker")
    void drawsOnCombatDamageToPlaneswalker() {
        Permanent submersible = addSubmersibleReady(player1);
        submersible.setAnimatedUntilEndOfTurn(true);
        submersible.setAnimatedPower(2);
        submersible.setAnimatedToughness(3);
        submersible.setAttacking(true);

        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceWielderOfMysteries());
        jace.setCounterCount(CounterType.LOYALTY, 4);
        submersible.setAttackTarget(jace.getId());
        harness.setLibrary(player1, List.of(new SilentSubmersible(), new SilentSubmersible()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Cannot crew without enough creature power")
    void cannotCrewWithoutCreatures() {
        Permanent submersible = addSubmersibleReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, submersible)).isFalse();
    }

    @Test
    @DisplayName("Summoning-sick creatures can crew")
    void summoningSickCreatureCanCrew() {
        Permanent submersible = addSubmersibleReady(player1);
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, submersible)).isTrue();
    }

    private Permanent addSubmersibleReady(Player player) {
        return addCreatureReady(player, new SilentSubmersible());
    }
}
