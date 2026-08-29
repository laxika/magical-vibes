package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SavageGorilla.class, Forest.class, GrizzlyBears.class})
class SavageGorillaTest extends BaseCardTest {

    @Test
    void sacrificesItselfDebuffsTargetAndDraws() {
        addReadyGorilla();
        Permanent target = addFourFourBears(player2);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Savage Gorilla");
        harness.assertInGraveyard(player1, "Savage Gorilla");

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void debuffWearsOffAtCleanup() {
        addReadyGorilla();
        Permanent target = addFourFourBears(player2);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        addReadyGorilla();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Savage Gorilla");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyGorilla() {
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new SavageGorilla());
        gorilla.setSummoningSick(false);
        return gorilla;
    }

    private Permanent addFourFourBears(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(4);
        card.setToughness(4);
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
