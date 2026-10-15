package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DuskdaleWurm;
import com.github.laxika.magicalvibes.cards.t.TwilightMire;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IndigoFaerie.class, DuskdaleWurm.class, TwilightMire.class})
class IndigoFaerieTest extends BaseCardTest {

    @Test
    @DisplayName("{U}: target keeps its own colors and gains blue")
    void targetGainsBlueInAdditionToOtherColors() {
        harness.addToBattlefield(player1, new IndigoFaerie());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new DuskdaleWurm());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, wurm.getId());
        harness.passBothPriorities();

        // Green Duskdale Wurm stays green and additionally becomes blue.
        assertThat(gqs.getEffectiveColors(gd, wurm))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
    }

    @Test
    @DisplayName("A colorless noncreature permanent simply becomes blue")
    void colorlessTargetBecomesBlue() {
        harness.addToBattlefield(player1, new IndigoFaerie());
        Permanent mire = harness.addToBattlefieldAndReturn(player2, new TwilightMire());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, mire.getId());
        harness.passBothPriorities();

        // Twilight Mire has no colors; the additive grant gives it blue without replacing anything.
        assertThat(gqs.getEffectiveColors(gd, mire)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Added blue wears off at end of turn")
    void blueWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new IndigoFaerie());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new DuskdaleWurm());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, wurm.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, wurm)).contains(CardColor.BLUE);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectiveColors(gd, wurm))
                .containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Faerie can target itself")
    void tappedSummoningSickFaerieCanTargetItself() {
        Permanent faerie = harness.addToBattlefieldAndReturn(player1, new IndigoFaerie());
        faerie.setSummoningSick(true);
        faerie.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, faerie.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, faerie)).containsExactly(CardColor.BLUE);
        assertThat(faerie.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the blue activation cost")
    void activationRequiresBlueMana() {
        Permanent faerie = harness.addToBattlefieldAndReturn(player1, new IndigoFaerie());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, faerie.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves after Indigo Faerie leaves the battlefield")
    void abilityResolvesWithoutItsSource() {
        Permanent faerie = harness.addToBattlefieldAndReturn(player1, new IndigoFaerie());
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new DuskdaleWurm());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, wurm.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, faerie));
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, wurm))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
    }

    @Test
    @DisplayName("An ability targeting a departed permanent does not affect its replacement")
    void departedTargetDoesNotColorANewPermanent() {
        harness.addToBattlefield(player1, new IndigoFaerie());
        Permanent original = harness.addToBattlefieldAndReturn(player2, new DuskdaleWurm());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, original.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, original));
        Permanent replacement = harness.addToBattlefieldAndReturn(player2, new DuskdaleWurm());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, replacement)).containsExactly(CardColor.GREEN);
        assertThat(gd.stack).isEmpty();
    }

}
