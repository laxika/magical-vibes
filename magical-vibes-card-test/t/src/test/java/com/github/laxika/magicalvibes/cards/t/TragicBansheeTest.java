package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TragicBanshee.class, BearCub.class})
class TragicBansheeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives an opponent's creature -1/-1 without morbid")
    void givesMinusOneMinusOneWithoutMorbid() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearCub());
        UUID targetId = target.getId();

        castTragicBanshee(targetId);
        resolveTragicBanshee();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("ETB gives an opponent's creature -13/-13 with morbid")
    void givesMinusThirteenMinusThirteenWithMorbid() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearCub());
        UUID targetId = target.getId();
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        castTragicBanshee(targetId);
        resolveTragicBanshee();

        harness.assertNotOnBattlefield(player2, "Bear Cub");
        harness.assertInGraveyard(player2, "Bear Cub");
    }

    @Test
    @DisplayName("Morbid is checked when the ETB resolves")
    void morbidCheckedAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearCub());
        UUID targetId = target.getId();

        castTragicBanshee(targetId);
        harness.passBothPriorities();
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bear Cub");
    }

    @Test
    @DisplayName("Cannot target your own creature")
    void cannotTargetOwnCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new BearCub()).getId();
        harness.setHand(player1, List.of(new TragicBanshee()));
        addManaForTragicBanshee();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The penalty expires at end of turn")
    void penaltyExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearCub());
        castTragicBanshee(target.getId());
        resolveTragicBanshee();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player2, "Bear Cub");
    }

    @Test
    @DisplayName("Can enter when the opponent has no creatures")
    void entersWithoutLegalTarget() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BearCub());

        harness.castFromHand(player1, new TragicBanshee(), "{4}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tragic Banshee");
        assertThat(gd.stack).isEmpty();
        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(ownCreature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Banshee dying in response enables morbid and replaces the base penalty")
    void sourceDeathEnablesMorbidAndReplacesBasePenalty() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearCub());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 12);
        castTragicBanshee(target.getId());
        harness.passBothPriorities();

        UUID sourceId = harness.getPermanentId(player1, "Tragic Banshee");
        Permanent source = harness.getGameQueryService().findPermanentById(gd, sourceId);
        source.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Tragic Banshee");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Bear Cub");
        assertThat(target.getPowerModifier()).isEqualTo(-13);
        assertThat(target.getToughnessModifier()).isEqualTo(-13);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    private void castTragicBanshee(UUID targetId) {
        harness.setHand(player1, List.of(new TragicBanshee()));
        addManaForTragicBanshee();
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void resolveTragicBanshee() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addManaForTragicBanshee() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

}
