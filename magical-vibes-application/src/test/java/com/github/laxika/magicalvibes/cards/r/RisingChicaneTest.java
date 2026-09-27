package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RisingChicane.class})
class RisingChicaneTest extends BaseCardTest {

    @Test
    void entersTappedForStartingPlayer() {
        playLand(player1);

        assertThat(findChicane(player1).isTapped()).isTrue();
    }

    @Test
    void entersUntappedForNonStartingPlayer() {
        gd.startingPlayerId = player2.getId();
        playLand(player1);

        assertThat(findChicane(player1).isTapped()).isFalse();
    }

    @Test
    void tapsForColorlessMana() {
        Permanent chicane = harness.addToBattlefieldAndReturn(player1, new RisingChicane());
        chicane.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(chicane.isTapped()).isTrue();
    }

    @Test
    void animatesAtMaxSpeedUntilEndOfTurn() {
        Permanent chicane = harness.addToBattlefieldAndReturn(player1, new RisingChicane());
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, chicane)).isTrue();
        assertThat(gqs.isLand(gd, chicane)).isTrue();
        assertThat(gqs.getEffectivePower(gd, chicane)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, chicane)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, chicane)).contains(CardSubtype.CONSTRUCT);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, chicane)).isFalse();
    }

    @Test
    void doesNotAnimateBelowMaxSpeed() {
        Permanent chicane = harness.addToBattlefieldAndReturn(player1, new RisingChicane());
        gd.playerSpeeds.put(player1.getId(), 3);

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, chicane)).isFalse();
    }

    private void playLand(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, java.util.List.of(new RisingChicane()));
        gd.activePlayerId = player.getId();
        gd.currentStep = TurnStep.PRECOMBAT_MAIN;
        harness.clearPriorityPassed();
        harness.playLand(player, 0);
    }

    private void advanceToCombat(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findChicane(com.github.laxika.magicalvibes.model.Player player) {
        return findPermanent(player, "Rising Chicane");
    }
}
