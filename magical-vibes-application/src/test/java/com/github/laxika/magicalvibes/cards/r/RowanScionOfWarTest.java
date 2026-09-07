package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.w.WallOfBlood;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RowanScionOfWar.class, WallOfBlood.class, LightningStrike.class})
class RowanScionOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces a matching spell by life lost this turn")
    void reducesMatchingSpellByLifeLostThisTurn() {
        addCreatureReady(player1, new WallOfBlood());
        addCreatureReady(player1, new RowanScionOfWar());
        harness.setLife(player1, 20);
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Snapshots life lost when the ability resolves")
    void snapshotsLifeLostWhenAbilityResolves() {
        addCreatureReady(player1, new WallOfBlood());
        addCreatureReady(player1, new RowanScionOfWar());
        harness.setLife(player1, 20);
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not reduce a matching spell when no life was lost")
    void doesNotReduceWithoutLifeLoss() {
        addCreatureReady(player1, new RowanScionOfWar());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
