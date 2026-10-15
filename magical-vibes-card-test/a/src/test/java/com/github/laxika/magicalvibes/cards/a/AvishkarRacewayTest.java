package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvishkarRaceway.class, Forest.class})
class AvishkarRacewayTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        addRaceway(player1);
        forceSorcerySpeed(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void maxSpeedAbilityDiscardsAndDraws() {
        addRaceway(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gd.playerSpeeds.put(player1.getId(), 4);
        forceSorcerySpeed(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void cannotActivateBelowMaxSpeed() {
        Permanent raceway = addRaceway(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gd.playerSpeeds.put(player1.getId(), 3);
        forceSorcerySpeed(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
        assertThat(raceway).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    void playingRacewayStartsOnlyItsControllersEngines() {
        harness.setHand(player1, List.of(new AvishkarRaceway()));
        forceSorcerySpeed(player1);

        harness.playLand(player1, 0);

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerSpeeds.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void enteringRacewayDoesNotResetExistingSpeed() {
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new AvishkarRaceway()));
        forceSorcerySpeed(player1);

        harness.playLand(player1, 0);

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void speedIncreasesOnlyOnceWhenOpponentLosesLifeOnControllersTurn() {
        addRaceway(player1);
        forceSorcerySpeed(player1);
        harness.runStateBasedActions();

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 1, "Life loss"));
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player2.getId(), 1, "Life loss"));
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player2.getId(), 1, "Life loss"));
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void opponentsLifeLossOnTheirTurnDoesNotIncreaseSpeed() {
        addRaceway(player1);
        forceSorcerySpeed(player2);
        harness.runStateBasedActions();

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player2.getId(), 1, "Life loss"));

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotLootBeforeSpeedIncreaseTriggerResolves() {
        Permanent raceway = addRaceway(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gd.playerSpeeds.put(player1.getId(), 3);
        forceSorcerySpeed(player1);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player2.getId(), 1, "Life loss"));

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
        assertThat(raceway.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void canLootOnOpponentsTurnAndPaysCostsBeforeDrawing() {
        Permanent raceway = addRaceway(player1);
        Forest discarded = new Forest();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gd.playerSpeeds.put(player1.getId(), 4);
        forceSorcerySpeed(player2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(raceway.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void cannotLootWithoutACardToDiscard() {
        Permanent raceway = addRaceway(player1);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gd.playerSpeeds.put(player1.getId(), 4);
        forceSorcerySpeed(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("discard");

        assertThat(raceway.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotLootWithTappedRaceway() {
        Permanent raceway = addRaceway(player1);
        raceway.tap();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gd.playerSpeeds.put(player1.getId(), 4);
        forceSorcerySpeed(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addRaceway(Player player) {
        return harness.addToBattlefieldAndReturn(player, new AvishkarRaceway());
    }

    private void forceSorcerySpeed(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
