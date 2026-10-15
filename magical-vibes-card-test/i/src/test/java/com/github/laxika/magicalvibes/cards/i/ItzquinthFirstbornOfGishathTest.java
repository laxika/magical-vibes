package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.ArmoredKincaller;
import com.github.laxika.magicalvibes.cards.s.SunshotMilitia;
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

@CardUsed({ItzquinthFirstbornOfGishath.class, ArmoredKincaller.class, SunshotMilitia.class})
class ItzquinthFirstbornOfGishathTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} makes a Dinosaur deal damage equal to its power to another creature")
    void payingTwoDealsDinosaurPowerDamage() {
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new ArmoredKincaller());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SunshotMilitia());

        castItzquinth(dinosaur, target, true);

        harness.assertInGraveyard(player1, "Sunshot Militia");
        harness.assertOnBattlefield(player1, "Armored Kincaller");
        assertThat(dinosaur.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining the payment does not deal damage")
    void decliningPaymentDoesNothing() {
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new ArmoredKincaller());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SunshotMilitia());

        castItzquinth(dinosaur, target, false);

        harness.assertOnBattlefield(player1, "Sunshot Militia");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The first target must be a Dinosaur you control")
    void firstTargetMustBeControlledDinosaur() {
        Permanent nonDinosaur = harness.addToBattlefieldAndReturn(player1, new SunshotMilitia());
        Permanent opposingDinosaur = harness.addToBattlefieldAndReturn(player2, new ArmoredKincaller());

        castItzquinth(null, null, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .contains(harness.getPermanentId(player1, "Itzquinth, Firstborn of Gishath"))
                .doesNotContain(nonDinosaur.getId(), opposingDinosaur.getId());
    }

    @Test
    @DisplayName("The second target must be another creature")
    void secondTargetMustBeAnotherCreature() {
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new ArmoredKincaller());
        castItzquinth(dinosaur, null, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, dinosaur.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Itzquinth can deal its own power as damage to an opponent's creature")
    void enteringItzquinthCanBeDamageSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SunshotMilitia());

        castItzquinth(null, null, true);
        harness.handlePermanentChosen(player1,
                harness.getPermanentId(player1, "Itzquinth, Firstborn of Gishath"));
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Sunshot Militia");
    }

    @Test
    @DisplayName("Declining payment requires no targets even when there is no other creature")
    void canDeclineWithoutAnotherCreature() {
        castItzquinth(null, null, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Itzquinth, Firstborn of Gishath");
    }

    @Test
    @DisplayName("Payment resolves before targets are selected")
    void paymentPrecedesTargetSelection() {
        harness.addToBattlefield(player2, new SunshotMilitia());

        castItzquinth(null, null, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
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
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, pay);

        if (!pay) {
            return;
        }

        if (dinosaur != null) {
            harness.handlePermanentChosen(player1, dinosaur.getId());
        }
        if (target != null) {
            harness.handlePermanentChosen(player1, target.getId());
            assertThat(target.getMarkedDamage()).isZero();
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
        }
    }
}
