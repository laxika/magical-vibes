package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncubationSacTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three oil counters")
    void entersWithThreeOilCounters() {
        harness.setHand(player1, List.of(new IncubationSac()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent sac = findPermanent(player1, "Incubation Sac");
        assertThat(sac.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing an oil counter creates a 3/3 Phyrexian Golem artifact creature token")
    void removingOilCounterCreatesGolemToken() {
        Permanent sac = addReadySac(player1, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sac.getCounterCount(CounterType.OIL)).isZero();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.GOLEM))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("The ability taps the source and removes only one oil counter")
    void abilityTapsSourceAndRemovesOneCounter() {
        Permanent sac = addReadySac(player1, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sac.isTapped()).isTrue();
        assertThat(sac.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability can only be activated as a sorcery")
    void abilityRequiresSorceryTiming() {
        addReadySac(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
    }

    @Test
    @DisplayName("The ability cannot be activated without an oil counter")
    void cannotActivateWithoutOilCounter() {
        addReadySac(player1, 0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    private Permanent addReadySac(Player player, int counters) {
        Permanent sac = new Permanent(new IncubationSac());
        sac.setSummoningSick(false);
        sac.setCounterCount(CounterType.OIL, counters);
        gd.playerBattlefields.get(player.getId()).add(sac);
        return sac;
    }
}
