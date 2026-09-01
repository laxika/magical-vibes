package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshnodsCylix;
import com.github.laxika.magicalvibes.cards.s.ShieldSphere;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Phyrexian Devourer")
@CardUsed({PhyrexianDevourer.class, AshnodsCylix.class, ShieldSphere.class})
class PhyrexianDevourerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a card puts +1/+1 counters equal to its mana value")
    void putsCountersEqualToManaValue() {
        Permanent devourer = addCreatureReady(player1, new PhyrexianDevourer());
        gd.playerDecks.get(player1.getId()).addFirst(new AshnodsCylix());
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(devourer.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, devourer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, devourer)).isEqualTo(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        harness.assertOnBattlefield(player1, "Phyrexian Devourer");
    }

    @Test
    @DisplayName("Exiling a zero mana value card puts no counters")
    void putsNoCountersForZeroManaValue() {
        Permanent devourer = addCreatureReady(player1, new PhyrexianDevourer());
        gd.playerDecks.get(player1.getId()).addFirst(new ShieldSphere());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(devourer.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
        assertThat(gqs.getEffectivePower(gd, devourer)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificed once its power reaches 7")
    void sacrificedAtPowerSeven() {
        Permanent devourer = addCreatureReady(player1, new PhyrexianDevourer());
        for (int i = 0; i < 3; i++) {
            gd.playerDecks.get(player1.getId()).addFirst(new AshnodsCylix());
        }

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            harness.clearPriorityPassed();
        }

        assertThat(gqs.getEffectivePower(gd, devourer)).isEqualTo(7);
        harness.passBothPriorities(); // resolve the state-triggered ability

        harness.assertNotOnBattlefield(player1, "Phyrexian Devourer");
        harness.assertInGraveyard(player1, "Phyrexian Devourer");
    }

    @Test
    @DisplayName("Each activation uses the mana value of its own exiled card")
    void eachActivationUsesItsOwnExiledCard() {
        Permanent devourer = addCreatureReady(player1, new PhyrexianDevourer());
        gd.playerDecks.get(player1.getId()).addFirst(new ShieldSphere());
        gd.playerDecks.get(player1.getId()).addFirst(new AshnodsCylix());

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(devourer.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, devourer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate with an empty library")
    void cannotActivateWithEmptyLibrary() {
        addCreatureReady(player1, new PhyrexianDevourer());
        gd.playerDecks.get(player1.getId()).clear();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in library to exile");
    }

}
