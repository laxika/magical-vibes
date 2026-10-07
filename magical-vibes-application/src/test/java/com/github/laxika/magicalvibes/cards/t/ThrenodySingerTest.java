package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrenodySinger.class, AirElemental.class, FugitiveWizard.class})
class ThrenodySingerTest extends BaseCardTest {

    @Test
    void etbGivesOpponentCreatureMinusPowerEqualToBlueDevotion() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player1, new FugitiveWizard());

        castSinger(target);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void onlyControllersBlueDevotionIsCounted() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player2, new FugitiveWizard());

        castSinger(target);

        assertThat(target.getEffectivePower()).isEqualTo(3);
    }

    @Test
    void debuffExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player1, new FugitiveWizard());

        castSinger(target);
        assertThat(target.getEffectivePower()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
    }

    @Test
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new ThrenodySinger()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void devotionIsCountedAtResolutionAndDoesNotChangeAfterward() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ThrenodySinger());
        castSingerWithoutResolving(target);
        harness.passBothPriorities();

        Permanent additionalSinger = harness.addToBattlefieldAndReturn(player1, new ThrenodySinger());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(-1);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(additionalSinger);
        assertThat(target.getEffectivePower()).isEqualTo(-1);
    }

    @Test
    void removedSourceDoesNotCountTowardDevotionWhenTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ThrenodySinger());
        castSingerWithoutResolving(target);
        harness.passBothPriorities();

        Permanent singer = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(singer);
        gd.playerGraveyards.get(player1.getId()).add(singer.getCard());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canBeCastDuringOpponentsTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ThrenodySinger());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        castSinger(target);

        harness.assertOnBattlefield(player1, "Threnody Singer");
        assertThat(target.getEffectivePower()).isZero();
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void canEnterWhenOpponentControlsNoCreatures() {
        harness.setHand(player1, List.of(new ThrenodySinger()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Threnody Singer");
        assertThat(gd.stack).isEmpty();
    }

    private void castSinger(Permanent target) {
        castSingerWithoutResolving(target);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castSingerWithoutResolving(Permanent target) {
        harness.setHand(player1, List.of(new ThrenodySinger()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, target.getId());
    }
}
