package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpiteMaliceTest extends BaseCardTest {

    private static final int SPITE = 0;
    private static final int MALICE = 1;

    @Test
    @DisplayName("Spite counters a noncreature spell")
    void spiteCountersNoncreatureSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player2, List.of(might));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.setHand(player1, List.of(new SpiteMalice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, bears.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, SPITE, might.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Might of Oaks");
    }

    @Test
    @DisplayName("Spite cannot target a creature spell")
    void spiteCannotTargetCreatureSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.setHand(player1, List.of(new SpiteMalice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, SPITE, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Malice destroys a nonblack creature without allowing regeneration")
    void maliceDestroysNonblackCreatureWithoutRegeneration() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setRegenerationShield(1);

        harness.setHand(player1, List.of(new SpiteMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, MALICE, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Malice cannot target a black creature")
    void maliceCannotTargetBlackCreature() {
        Permanent ghouls = harness.addToBattlefieldAndReturn(player2, new MassOfGhouls());

        harness.setHand(player1, List.of(new SpiteMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, MALICE, ghouls.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
