package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeflectingSwat.class, Boomerang.class, GrizzlyBears.class, IcyManipulator.class})
class DeflectingSwatTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for free while its controller controls a commander")
    void castsForFreeWithCommander() {
        Permanent commander = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        commander.setCommander(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Boomerang boomerang = new Boomerang();

        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeflectingSwat()));
        harness.castInstantWithAlternateCost(player2, 0, boomerang.getId(), List.of());

        assertThat(harness.getGameData().stack).hasSize(2);
        assertThat(harness.getGameData().stack.getLast().getCard().getName()).isEqualTo("Deflecting Swat");
    }

    @Test
    @DisplayName("Cannot use the free cast without controlling a commander")
    void cannotCastForFreeWithoutCommander() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Boomerang boomerang = new Boomerang();

        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeflectingSwat()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player2, 0, boomerang.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can retarget an activated ability")
    void retargetsActivatedAbility() {
        IcyManipulator icyManipulator = new IcyManipulator();
        harness.addToBattlefieldAndReturn(player1, icyManipulator);
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        newTarget.setCommander(true);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, originalTarget.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeflectingSwat()));
        harness.castInstantWithAlternateCost(player2, 0, icyManipulator.getId(), List.of());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(newTarget.getId())
                .doesNotContain(originalTarget.getId());

        harness.handlePermanentChosen(player2, newTarget.getId());

        StackEntry ability = gd.stack.getLast();
        assertThat(ability.getTargetId()).isEqualTo(newTarget.getId());
    }
}
