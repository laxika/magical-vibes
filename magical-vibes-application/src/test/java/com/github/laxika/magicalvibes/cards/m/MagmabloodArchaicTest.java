package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MagmabloodArchaicTest extends BaseCardTest {

    @Test
    @DisplayName("Converge puts one +1/+1 counter on it for each color spent")
    void convergeCountsColorsSpentToCastIt() {
        harness.setHand(player1, List.of(new MagmabloodArchaic()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent archaic = findPermanent(player1, "Magmablood Archaic");
        assertThat(archaic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, archaic)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, archaic)).isEqualTo(7);
    }

    @Test
    @DisplayName("Instant and sorcery casts boost all your creatures by each color spent")
    void instantOrSorceryCastBoostsAllOwnCreatures() {
        Permanent archaic = addCreatureReady(player1, new MagmabloodArchaic());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, archaic)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Repeated mana of one color and colorless mana count as one color")
    void repeatedAndColorlessManaCountCorrectly() {
        Permanent archaic = addCreatureReady(player1, new MagmabloodArchaic());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, archaic)).isEqualTo(3);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the team boost")
    void creatureCastDoesNotTrigger() {
        Permanent archaic = addCreatureReady(player1, new MagmabloodArchaic());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, archaic)).isEqualTo(2);
    }
}
