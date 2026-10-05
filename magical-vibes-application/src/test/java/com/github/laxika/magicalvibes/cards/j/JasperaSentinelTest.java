package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.l.LittjaraGladeWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JasperaSentinel.class, LittjaraGladeWarden.class})
class JasperaSentinelTest extends BaseCardTest {

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void canTapSummoningSickHelperForAnyColor(ManaColor color) {
        Permanent sentinel = addCreatureReady(player1, new JasperaSentinel());
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new LittjaraGladeWarden());
        helper.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(sentinel.isTapped()).isTrue();
        assertThat(helper.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickSentinelCannotActivate() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new JasperaSentinel());
        sentinel.setSummoningSick(true);
        Permanent helper = addCreatureReady(player1, new LittjaraGladeWarden());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(sentinel.isTapped()).isFalse();
        assertThat(helper.isTapped()).isFalse();
    }

    @Test
    void tappedAndOpposingCreaturesCannotPayCost() {
        Permanent sentinel = addCreatureReady(player1, new JasperaSentinel());
        Permanent tappedHelper = addCreatureReady(player1, new LittjaraGladeWarden());
        tappedHelper.tap();
        Permanent opposingHelper = addCreatureReady(player2, new LittjaraGladeWarden());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(sentinel.isTapped()).isFalse();
        assertThat(opposingHelper.isTapped()).isFalse();
    }

    @Test
    void tappedSentinelCannotActivateAgain() {
        Permanent sentinel = addCreatureReady(player1, new JasperaSentinel());
        sentinel.tap();
        Permanent helper = addCreatureReady(player1, new LittjaraGladeWarden());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(helper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Taps itself and another creature, then adds one mana of the chosen color")
    void tapsItselfAndAnotherCreatureForMana() {
        Permanent sentinel = addCreatureReady(player1, new JasperaSentinel());
        Permanent helper = addCreatureReady(player1, new LittjaraGladeWarden());

        int sentinelIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sentinel);
        harness.activateAbility(player1, sentinelIndex, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(sentinel.isTapped()).isTrue();
        assertThat(helper.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Prompts which other creature to tap when multiple are available")
    void promptsForCreatureChoice() {
        Permanent sentinel = addCreatureReady(player1, new JasperaSentinel());
        Permanent firstHelper = addCreatureReady(player1, new LittjaraGladeWarden());
        Permanent secondHelper = addCreatureReady(player1, new LittjaraGladeWarden());

        int sentinelIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sentinel);
        harness.activateAbility(player1, sentinelIndex, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstHelper.getId());
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(sentinel.isTapped()).isTrue();
        assertThat(firstHelper.isTapped()).isTrue();
        assertThat(secondHelper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without another untapped creature")
    void cannotActivateWithoutAnotherUntappedCreature() {
        Permanent sentinel = addCreatureReady(player1, new JasperaSentinel());
        int sentinelIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sentinel);

        assertThatThrownBy(() -> harness.activateAbility(player1, sentinelIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }
}

