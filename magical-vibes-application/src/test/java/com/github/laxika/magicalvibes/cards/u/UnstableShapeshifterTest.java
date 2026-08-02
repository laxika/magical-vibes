package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UnstableShapeshifterTest extends BaseCardTest {

    private Permanent putShapeshifter() {
        Permanent shifter = new Permanent(new UnstableShapeshifter());
        gd.playerBattlefields.get(player1.getId()).add(shifter);
        return shifter;
    }

    @Test
    @DisplayName("Becomes a copy of another creature that enters")
    void becomesCopyOfEnteringCreature() {
        Permanent shifter = putShapeshifter();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Grizzly Bears resolves, trigger goes on the stack
        harness.passBothPriorities(); // become-copy resolves

        assertThat(shifter.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, shifter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shifter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Retains the copy ability — copies again when another creature enters")
    void retainsAbilityAndCopiesAgain() {
        Permanent shifter = putShapeshifter();

        harness.setHand(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(shifter.getCard().getName()).isEqualTo("Grizzly Bears");

        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shifter.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.getEffectivePower(gd, shifter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shifter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Copies a creature entering under an opponent's control")
    void copiesOpponentCreature() {
        Permanent shifter = putShapeshifter();

        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shifter.getCard().getName()).isEqualTo("Hill Giant");
    }

    @Test
    @DisplayName("Does not trigger on itself entering")
    void doesNotTriggerOnItself() {
        harness.setHand(player1, List.of(new UnstableShapeshifter()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent shifter = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(shifter.getCard().getName()).isEqualTo("Unstable Shapeshifter");
    }
}
