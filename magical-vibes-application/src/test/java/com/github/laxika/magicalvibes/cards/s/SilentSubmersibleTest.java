package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilentSubmersible.class, GrizzlyBears.class})
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
        Permanent crew = addReadyCreature(player1, new GrizzlyBears());

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

        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    private Permanent addSubmersibleReady(Player player) {
        return addReadyPermanent(player, new SilentSubmersible());
    }

    private Permanent addReadyCreature(Player player, Card card) {
        return addReadyPermanent(player, card);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
