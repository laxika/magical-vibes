package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlabornMusketeer;
import com.github.laxika.magicalvibes.cards.e.Extinguish;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TouchOfBrilliance;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlabornMusketeer.class, Extinguish.class, GrizzlyBears.class, MysticDenial.class, TouchOfBrilliance.class})
class MysticDenialTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell")
    void countersCreatureSpell() {
        AlabornMusketeer musketeer = new AlabornMusketeer();
        harness.castFromHand(player1, musketeer, "{1}{W}");

        harness.setHand(player2, List.of(new MysticDenial()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, musketeer.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alaborn Musketeer");
        harness.assertNotOnBattlefield(player1, "Alaborn Musketeer");
    }

    @Test
    @DisplayName("Counters a sorcery spell")
    void countersSorcerySpell() {
        TouchOfBrilliance brilliance = new TouchOfBrilliance();
        harness.castFromHand(player1, brilliance, "{3}{U}");

        harness.setHand(player2, List.of(new MysticDenial()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, brilliance.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Touch of Brilliance");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an instant spell")
    void cannotTargetInstantSpell() {
        TouchOfBrilliance brilliance = new TouchOfBrilliance();
        harness.castFromHand(player1, brilliance, "{3}{U}");

        Extinguish extinguish = new Extinguish();
        harness.setHand(player1, List.of(extinguish));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, brilliance.getId());

        harness.setHand(player2, List.of(new MysticDenial()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, extinguish.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target spell leaves the stack before resolution")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        MysticDenial mysticDenial = new MysticDenial();
        harness.setHand(player2, List.of(mysticDenial));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        gd.stack.removeIf(entry -> entry.getCard().getId().equals(bears.getId()));

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player2, "Mystic Denial");
        assertThat(gd.stack).isEmpty();
    }
}
