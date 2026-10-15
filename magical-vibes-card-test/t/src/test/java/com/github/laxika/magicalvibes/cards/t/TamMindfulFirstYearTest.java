package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrubsCommand;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TamMindfulFirstYear.class, GrizzlyBears.class, GiantGrowth.class,
        GrubsCommand.class, ProdigalPyromancer.class, Shock.class})
class TamMindfulFirstYearTest extends BaseCardTest {

    @Test
    @DisplayName("Gives each other creature hexproof from its own colors")
    void givesEachOtherCreatureHexproofFromItsOwnColors() {
        Permanent tam = addCreatureReady(player1, new TamMindfulFirstYear());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasHexproofFromColor(gd, bears, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasHexproofFromColor(gd, bears, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasHexproofFromColor(gd, tam, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("The color-granting ability makes the target all colors and tracks the change")
    void colorGrantMakesTargetAllColorsAndTracksTheChange() {
        addCreatureReady(player1, new TamMindfulFirstYear());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, bears))
                .containsExactlyInAnyOrder(
                        CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
        assertThat(gqs.hasHexproofFromColor(gd, bears, CardColor.BLUE)).isTrue();

        gd.expireEndOfTurnFloatingEffects();
        bears.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasHexproofFromColor(gd, bears, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Rejects an opposing spell with one of the creature's colors")
    void rejectsOpposingSpellWithCreatureColor() {
        addCreatureReady(player1, new TamMindfulFirstYear());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotProtectOpponentsCreatures() {
        addCreatureReady(player1, new TamMindfulFirstYear());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasHexproofFromColor(gd, bears, CardColor.GREEN)).isFalse();
    }

    @Test
    void allowsItsControllerToTargetAProtectedCreature() {
        addCreatureReady(player1, new TamMindfulFirstYear());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    void canTargetItselfWithoutGrantingItselfHexproof() {
        Permanent tam = addCreatureReady(player1, new TamMindfulFirstYear());

        harness.activateAbility(player1, 0, null, tam.getId());
        harness.passBothPriorities();

        assertThat(tam.isTapped()).isTrue();
        assertThat(gqs.getEffectiveColors(gd, tam)).containsExactlyInAnyOrder(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasHexproofFromColor(gd, tam, color)).isFalse();
        }
    }

    @Test
    void cannotMakeAnOpponentsCreatureAllColors() {
        addCreatureReady(player1, new TamMindfulFirstYear());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateItsTapAbilityWhileSummoningSick() {
        Permanent tam = harness.addToBattlefieldAndReturn(player1, new TamMindfulFirstYear());
        tam.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, tam.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedColorChangeResolvesAfterTamDiesButHexproofEnds() {
        Permanent tam = addCreatureReady(player1, new TamMindfulFirstYear());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.castInstant(player2, 0, tam.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tam, Mindful First-Year");
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactlyInAnyOrder(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasHexproofFromColor(gd, bears, color)).isFalse();
        }
    }

    @Test
    void gainingAllColorsInResponseMakesAnOpposingRedSpellFailToResolve() {
        addCreatureReady(player1, new TamMindfulFirstYear());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void rejectsAnAbilityFromASourceThatHasBecomeGreen() {
        addCreatureReady(player1, new TamMindfulFirstYear());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new TamMindfulFirstYear());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, 0, null, pyromancer.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player2, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsAMulticoloredSpellSharingAnyProtectedColor() {
        addCreatureReady(player1, new TamMindfulFirstYear());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrubsCommand()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player2, 0, 2,
                new int[]{1, 2}, List.of(player2.getId(), pyromancer.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
