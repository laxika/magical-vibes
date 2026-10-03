package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AstralCornucopia.class})
class AstralCornucopiaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X charge counters and adds the chosen color per counter")
    void entersWithXChargeCountersAndProducesChosenColor() {
        harness.setHand(player1, List.of(new AstralCornucopia()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveSorcery(player1, 0, 2);

        Permanent cornucopia = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cornucopia.getCounterCount(CounterType.CHARGE)).isEqualTo(2);

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Produces no mana with no charge counters")
    void producesNoManaWithNoChargeCounters() {
        harness.addToBattlefield(player1, new AstralCornucopia());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can be cast for zero and enters without charge counters")
    void canBeCastForZero() {
        harness.setHand(player1, List.of(new AstralCornucopia()));

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent cornucopia = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cornucopia.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Uses current charge counters and produces the entire batch in one chosen color")
    void producesCurrentChargeCountInChosenColor(ManaColor color) {
        harness.setHand(player1, List.of(new AstralCornucopia()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 1);
        Permanent cornucopia = gd.playerBattlefields.get(player1.getId()).getFirst();
        cornucopia.setCounterCount(CounterType.CHARGE, 4);
        cornucopia.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        for (ManaColor poolColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(poolColor))
                    .isEqualTo(poolColor == color ? 4 : 0);
        }
        assertThat(cornucopia.isTapped()).isTrue();
        assertThat(cornucopia.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
