package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.t.TerritorialRoc;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DromokaDunecaster.class, TerritorialRoc.class, DanceOfTheSkywise.class})
class DromokaDunecasterTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature without flying and taps itself")
    void tapsTargetCreatureWithoutFlying() {
        Permanent dunecaster = addReadyPermanent(player1, new DromokaDunecaster());
        Permanent target = addReadyPermanent(player2, new DromokaDunecaster());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(dunecaster.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetCreatureWithFlying() {
        addReadyPermanent(player1, new DromokaDunecaster());
        Permanent target = addReadyPermanent(player2, new TerritorialRoc());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without flying");
    }

    @Test
    void canTapOwnCreature() {
        addReadyPermanent(player1, new DromokaDunecaster());
        Permanent target = addReadyPermanent(player1, new DromokaDunecaster());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canTargetItself() {
        Permanent dunecaster = addReadyPermanent(player1, new DromokaDunecaster());
        addActivationMana();

        harness.activateAbility(player1, 0, null, dunecaster.getId());
        assertThat(dunecaster.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(dunecaster.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent dunecaster = addReadyPermanent(player1, new DromokaDunecaster());
        dunecaster.setSummoningSick(true);
        Permanent target = addReadyPermanent(player2, new DromokaDunecaster());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dunecaster.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent dunecaster = addReadyPermanent(player1, new DromokaDunecaster());
        dunecaster.setTapped(true);
        Permanent target = addReadyPermanent(player2, new DromokaDunecaster());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithOnlyOneWhiteMana() {
        Permanent dunecaster = addReadyPermanent(player1, new DromokaDunecaster());
        Permanent target = addReadyPermanent(player2, new DromokaDunecaster());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dunecaster.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTapTargetThatGainsFlyingBeforeResolution() {
        Permanent dunecaster = addReadyPermanent(player1, new DromokaDunecaster());
        Permanent target = addReadyPermanent(player2, new DromokaDunecaster());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.setHand(player2, List.of(new DanceOfTheSkywise()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(dunecaster.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent dunecaster = addReadyPermanent(player1, new DromokaDunecaster());
        Permanent target = addReadyPermanent(player2, new DromokaDunecaster());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(dunecaster);
        gd.playerGraveyards.get(player1.getId()).add(dunecaster.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
