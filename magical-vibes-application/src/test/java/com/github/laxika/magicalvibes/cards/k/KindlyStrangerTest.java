package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AimHigh;
import com.github.laxika.magicalvibes.cards.d.DrownyardExplorers;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KindlyStranger.class, DrownyardExplorers.class, Forest.class, AimHigh.class, MagnifyingGlass.class})
class KindlyStrangerTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot transform without delirium")
    void cannotTransformWithoutDelirium() {
        harness.setGraveyard(player1, List.of(new DrownyardExplorers(), new Forest(), new AimHigh()));
        harness.addToBattlefield(player1, new KindlyStranger());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Transforms with four card types in the graveyard")
    void transformsWithDelirium() {
        harness.setGraveyard(player1, List.of(
                new DrownyardExplorers(), new Forest(), new AimHigh(), new MagnifyingGlass()));
        harness.addToBattlefield(player1, new KindlyStranger());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent stranger = findPermanent(player1, "Demon-Possessed Witch");
        assertThat(stranger.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, stranger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stranger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Transform trigger may destroy target creature")
    void transformTriggerMayDestroyTargetCreature() {
        harness.setGraveyard(player1, List.of(
                new DrownyardExplorers(), new Forest(), new AimHigh(), new MagnifyingGlass()));
        harness.addToBattlefield(player1, new KindlyStranger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrownyardExplorers());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Drownyard Explorers");
        harness.assertInGraveyard(player2, "Drownyard Explorers");
    }

    @Test
    @DisplayName("Transform trigger may be declined")
    void transformTriggerMayBeDeclined() {
        harness.setGraveyard(player1, List.of(
                new DrownyardExplorers(), new Forest(), new AimHigh(), new MagnifyingGlass()));
        harness.addToBattlefield(player1, new KindlyStranger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrownyardExplorers());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(findPermanent(player1, "Demon-Possessed Witch").isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Losing delirium after activation does not prevent transformation")
    void losingDeliriumAfterActivationStillTransforms() {
        harness.setGraveyard(player1, List.of(
                new DrownyardExplorers(), new Forest(), new AimHigh(), new MagnifyingGlass()));
        Permanent stranger = harness.addToBattlefieldAndReturn(player1, new KindlyStranger());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, stranger.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(stranger.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Demon-Possessed Witch");
    }

    @Test
    @DisplayName("Two stacked activations transform only once")
    void stackedActivationsTransformOnlyOnce() {
        harness.setGraveyard(player1, List.of(
                new DrownyardExplorers(), new Forest(), new AimHigh(), new MagnifyingGlass()));
        Permanent stranger = harness.addToBattlefieldAndReturn(player1, new KindlyStranger());
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, stranger.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(stranger.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Demon-Possessed Witch");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The transform trigger can destroy the Witch itself")
    void transformTriggerCanDestroyItself() {
        harness.setGraveyard(player1, List.of(
                new DrownyardExplorers(), new Forest(), new AimHigh(), new MagnifyingGlass()));
        Permanent stranger = harness.addToBattlefieldAndReturn(player1, new KindlyStranger());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, stranger.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Demon-Possessed Witch");
        harness.assertInGraveyard(player1, "Kindly Stranger");
    }

    @Test
    @DisplayName("Delirium counts only the controller's graveyard")
    void opponentsGraveyardDoesNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(new DrownyardExplorers(), new Forest(), new AimHigh()));
        harness.setGraveyard(player2, List.of(new MagnifyingGlass()));
        harness.addToBattlefield(player1, new KindlyStranger());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Four cards with only three distinct card types do not enable delirium")
    void duplicateTypesDoNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(
                new DrownyardExplorers(), new DrownyardExplorers(), new Forest(), new AimHigh()));
        harness.addToBattlefield(player1, new KindlyStranger());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
