package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Storm Elemental")
class StormElementalTest extends BaseCardTest {

    @Test
    @DisplayName("First ability taps target creature with flying and exiles the top library card")
    void tapsFlyingTarget() {
        addElemental();
        Permanent flyer = addFlyer(player2);
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
        addElemental();
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability pumps when the exiled card is a snow land")
    void pumpsOnSnowLand() {
        Permanent elemental = addElemental();
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
        Permanent elemental = addElemental();
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
        Permanent elemental = addElemental();
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
        addElemental();
        gd.playerDecks.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in library to exile");
    }

    private Permanent addElemental() {
        Permanent elemental = new Permanent(new StormElemental());
        elemental.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elemental);
        return elemental;
    }

    private Permanent addFlyer(Player player) {
        Permanent flyer = new Permanent(new AirElemental());
        flyer.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(flyer);
        return flyer;
    }
}
