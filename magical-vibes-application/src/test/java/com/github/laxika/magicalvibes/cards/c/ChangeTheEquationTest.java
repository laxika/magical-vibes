package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChangeTheEquation.class, Divination.class, GrizzlyBears.class, MightOfOaks.class, Shock.class})
class ChangeTheEquationTest extends BaseCardTest {

    @Test
    void firstModeCountersSpellWithManaValueTwoOrLess() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new ChangeTheEquation()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castModalInstantWithModes(player1, 0, 1, new int[]{0}, shock.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void secondModeCountersRedOrGreenSpellWithManaValueSixOrLess() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);

        MightOfOaks mightOfOaks = new MightOfOaks();
        harness.setHand(player2, List.of(mightOfOaks));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.setHand(player1, List.of(new ChangeTheEquation()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passPriority(player2);
        harness.castModalInstantWithModes(player1, 0, 1, new int[]{1}, mightOfOaks.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Might of Oaks");
    }

    @Test
    void firstModeRejectsSpellWithManaValueAboveTwo() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);

        MightOfOaks mightOfOaks = new MightOfOaks();
        harness.setHand(player2, List.of(mightOfOaks));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.setHand(player1, List.of(new ChangeTheEquation()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, new int[]{0}, mightOfOaks.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secondModeRejectsSpellThatIsNeitherRedNorGreen() {
        Divination divination = new Divination();
        harness.setHand(player2, List.of(divination));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.setHand(player1, List.of(new ChangeTheEquation()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, new int[]{1}, divination.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
