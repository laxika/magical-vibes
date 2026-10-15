package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MuragandaRaceway.class})
class MuragandaRacewayTest extends BaseCardTest {

    @Test
    void startsEnginesAndIncreasesSpeedOnlyOncePerTurn() {
        addRaceway(player1);
        forceSorcerySpeed(player1);
        harness.runStateBasedActions();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1);
            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1);
        });

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void tapsForColorlessMana() {
        addRaceway(player1);
        forceSorcerySpeed(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void maxSpeedTapsForTwoColorlessMana() {
        addRaceway(player1);
        gd.playerSpeeds.put(player1.getId(), 4);
        forceSorcerySpeed(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void maxSpeedAbilityRequiresMaxSpeed() {
        addRaceway(player1);
        gd.playerSpeeds.put(player1.getId(), 3);
        forceSorcerySpeed(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
    }

    @Test
    void anotherRacewayDoesNotResetExistingSpeed() {
        addRaceway(player1);
        gd.playerSpeeds.put(player1.getId(), 3);
        addRaceway(player1);

        harness.runStateBasedActions();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void lifeLossOnOpponentsTurnDoesNotIncreaseSpeed() {
        addRaceway(player1);
        harness.runStateBasedActions();
        forceSorcerySpeed(player2);

        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1));
        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void controllersLifeLossDoesNotIncreaseSpeed() {
        addRaceway(player1);
        harness.runStateBasedActions();
        forceSorcerySpeed(player1);

        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player1.getId(), 1));
        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void maxSpeedAbilityUsesControllersSpeedRatherThanOpponents() {
        addRaceway(player1);
        gd.playerSpeeds.put(player1.getId(), 3);
        gd.playerSpeeds.put(player2.getId(), 4);
        forceSorcerySpeed(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void normalManaAbilityRemainsAvailableAtMaxSpeed() {
        addRaceway(player1);
        gd.playerSpeeds.put(player1.getId(), 4);
        forceSorcerySpeed(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void maxSpeedManaAbilityResolvesImmediatelyOnOpponentsTurnAndCannotBeUsedTwice() {
        addRaceway(player1);
        gd.playerSpeeds.put(player1.getId(), 4);
        forceSorcerySpeed(player2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    private void addRaceway(Player player) {
        harness.addToBattlefield(player, new MuragandaRaceway());
    }

    private void forceSorcerySpeed(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
