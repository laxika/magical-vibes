package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WindSpirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Storm Elemental")
@CardUsed({
        StormElemental.class, WindSpirit.class, BalduvianBears.class, Island.class, SnowCoveredIsland.class
})
class StormElementalTest extends BaseCardTest {

    @Test
    @DisplayName("First ability taps target creature with flying and exiles the top library card")
    void tapsFlyingTarget() {
        addCreatureReady(player1, new StormElemental());
        Permanent flyer = addCreatureReady(player2, new WindSpirit());
        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, flyer.getId());
        harness.passBothPriorities();

        assertThat(flyer.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("First ability cannot target a creature without flying")
    void cannotTargetNonFlyer() {
        addCreatureReady(player1, new StormElemental());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability pumps when the exiled card is a snow land")
    void pumpsOnSnowLand() {
        Permanent elemental = addCreatureReady(player1, new StormElemental());
        gd.playerDecks.get(player1.getId()).addFirst(new SnowCoveredIsland());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(5);
    }

    @Test
    @DisplayName("Second ability does not pump when the exiled card is a nonsnow land")
    void doesNotPumpOnNonsnowLand() {
        Permanent elemental = addCreatureReady(player1, new StormElemental());
        gd.playerDecks.get(player1.getId()).addFirst(new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
    }

    @Test
    @DisplayName("Second ability's boost wears off at end of turn")
    void boostWearsOff() {
        Permanent elemental = addCreatureReady(player1, new StormElemental());
        gd.playerDecks.get(player1.getId()).addFirst(new SnowCoveredIsland());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
    }

    @Test
    @DisplayName("Neither ability can be activated with an empty library")
    void cannotActivateWithEmptyLibrary() {
        addCreatureReady(player1, new StormElemental());
        Permanent flyer = addCreatureReady(player2, new WindSpirit());
        gd.playerDecks.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in library to exile");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in library to exile");
    }

    @Test
    @DisplayName("Each activation checks the card exiled for that activation")
    void eachActivationUsesItsOwnExiledCard() {
        Permanent elemental = addCreatureReady(player1, new StormElemental());
        gd.playerDecks.get(player1.getId()).addFirst(new Island());
        gd.playerDecks.get(player1.getId()).addFirst(new SnowCoveredIsland());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(5);
    }
}
