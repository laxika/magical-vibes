package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.cards.s.SuddenDeath;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({TraitorsClutch.class, AshcoatBear.class, PrismaticLens.class, SuddenDeath.class})
class TraitorsClutchTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +1/+0, becomes black, and gains shadow")
    void appliesTemporaryEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new TraitorsClutch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Temporary effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new TraitorsClutch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("Flashback applies the effect and exiles the spell")
    void flashbackAppliesEffectAndExilesSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.setGraveyard(player1, List.of(new TraitorsClutch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.SHADOW)).isTrue();
        harness.assertNotInGraveyard(player1, "Traitor's Clutch");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Traitor's Clutch"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PrismaticLens());
        harness.setHand(player1, List.of(new TraitorsClutch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Casting normally then flashing back stacks the power bonuses")
    void normalCastAndFlashbackStackBonuses() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new TraitorsClutch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInGraveyard(player1, "Traitor's Clutch");
        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);
        assertThat(gqs.hasKeyword(gd, target, Keyword.SHADOW)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, other)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasKeyword(gd, other, Keyword.SHADOW)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasKeyword(gd, target, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("Flashback still exiles the spell when its target leaves before resolution")
    void flashbackExilesWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.setGraveyard(player1, List.of(new TraitorsClutch()));
        harness.setHand(player2, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Traitor's Clutch");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Traitor's Clutch"));
    }
}
