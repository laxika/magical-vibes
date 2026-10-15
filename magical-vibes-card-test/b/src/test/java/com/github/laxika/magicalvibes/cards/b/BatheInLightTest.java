package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.cards.s.SnappingDrake;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BatheInLight.class, BorosRecruit.class, GlassGolem.class, SnappingDrake.class, Watchwolf.class})
class BatheInLightTest extends BaseCardTest {

    @Test
    @DisplayName("Protects the target and every creature sharing a color with it")
    void protectsTargetAndColorSharingCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        Permanent ownMatchingCreature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent opponentMatchingCreature = harness.addToBattlefieldAndReturn(player2, new Watchwolf());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new SnappingDrake());
        harness.setHand(player1, List.of(new BatheInLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, "RED");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(ownMatchingCreature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(opponentMatchingCreature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(differentColorCreature.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("A colorless target does not share a color with other colorless creatures")
    void colorlessTargetOnlyAffectsItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GlassGolem());
        Permanent otherColorlessCreature = harness.addToBattlefieldAndReturn(player2, new GlassGolem());
        Permanent coloredCreature = harness.addToBattlefieldAndReturn(player2, new Watchwolf());
        harness.setHand(player1, List.of(new BatheInLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "BLUE");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);
        assertThat(otherColorlessCreature.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
        assertThat(coloredCreature.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
    }

    @Test
    @DisplayName("Fizzles without a color choice if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        harness.setHand(player1, List.of(new BatheInLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("The caster chooses protection even when targeting an opponent's creature")
    void casterChoosesProtectionForOpponentTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Watchwolf());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        harness.setHand(player1, List.of(new BatheInLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "WHITE");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.WHITE);
        assertThat(matchingCreature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.WHITE);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("Radiance includes creatures present at resolution, but not later entrants")
    void affectedCreaturesAreDeterminedAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        harness.setHand(player1, List.of(new BatheInLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player2, new Watchwolf());

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
        assertThat(beforeResolution.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
        assertThat(afterResolution.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLACK);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        harness.setHand(player1, List.of(new BatheInLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "BLUE");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
    }
}
