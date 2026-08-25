package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ItzquinthFirstbornOfGishath.class, FrenziedRaptor.class, GrizzlyBears.class})
class ItzquinthFirstbornOfGishathTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} makes a Dinosaur deal damage equal to its power to another creature")
    void payingTwoDealsDinosaurPowerDamage() {
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new FrenziedRaptor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castItzquinth(dinosaur, target, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Frenzied Raptor");
    }

    @Test
    @DisplayName("Declining the payment does not deal damage")
    void decliningPaymentDoesNothing() {
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new FrenziedRaptor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castItzquinth(dinosaur, target, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The first target must be a Dinosaur you control")
    void firstTargetMustBeControlledDinosaur() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castItzquinth(null, null, false);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).doesNotContain(bear.getId());
    }

    @Test
    @DisplayName("The second target must be another creature")
    void secondTargetMustBeAnotherCreature() {
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new FrenziedRaptor());
        castItzquinth(dinosaur, null, false);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, dinosaur.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castItzquinth(Permanent dinosaur, Permanent target, boolean pay) {
        harness.setHand(player1, List.of(new ItzquinthFirstbornOfGishath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        if (pay) {
            harness.addMana(player1, ManaColor.COLORLESS, 2);
        }

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        if (dinosaur != null) {
            harness.handlePermanentChosen(player1, dinosaur.getId());
        }
        if (target != null) {
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, pay);
        }
    }
}
