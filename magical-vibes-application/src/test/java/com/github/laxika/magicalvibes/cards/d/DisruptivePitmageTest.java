package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisruptivePitmage.class, GrizzlyBears.class})
class DisruptivePitmageTest extends BaseCardTest {

    @Test
    void countersSpellWhenControllerCannotPay() {
        addCreatureReady(player2, new DisruptivePitmage());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void spellControllerMayPayOneMana() {
        addCreatureReady(player2, new DisruptivePitmage());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotTargetPermanent() {
        addCreatureReady(player2, new DisruptivePitmage());
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeMorphedFaceDownAndTurnedFaceUp() {
        harness.setHand(player1, List.of(new DisruptivePitmage()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent pitmage = findPermanent(player1, "Disruptive Pitmage");
        assertThat(pitmage.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pitmage));
        harness.passBothPriorities();

        assertThat(pitmage.isFaceDown()).isFalse();
    }
}
