package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DralnusPet;
import com.github.laxika.magicalvibes.cards.m.MeteorCrater;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuroraGriffin.class, DralnusPet.class, MeteorCrater.class, Terminate.class})
class AuroraGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Target permanent becomes white until end of turn")
    void targetPermanentBecomesWhiteUntilEndOfTurn() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AuroraGriffin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DralnusPet());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isFalse();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Can target a noncreature permanent")
    void canTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new AuroraGriffin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MeteorCrater());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).isEmpty();
    }

    @Test
    @DisplayName("A tapped Griffin can activate repeatedly for separate permanents")
    void tappedGriffinCanActivateRepeatedly() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AuroraGriffin());
        source.tap();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DralnusPet());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MeteorCrater());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.BLUE);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.WHITE);
        assertThat(gqs.getEffectiveColors(gd, land)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("The ability resolves after the Griffin is destroyed in response")
    void abilityResolvesAfterSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AuroraGriffin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DralnusPet());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.assertInGraveyard(player1, "Aurora Griffin");
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("The ability has no effect when its target is destroyed in response")
    void destroyedTargetIsNotAffected() {
        harness.addToBattlefield(player1, new AuroraGriffin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DralnusPet());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dralnu's Pet");
        harness.assertInGraveyard(player2, "Dralnu's Pet");
        assertThat(gd.stack).isEmpty();
    }
}
