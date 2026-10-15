package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.c.CivicWayfinder;
import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
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

@CardUsed({WojekSiren.class, CivicWayfinder.class, ViashinoFangtail.class, GlassGolem.class,
        BorosRecruit.class, CourierHawk.class, Forest.class})
class WojekSirenTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void doesNotBoostCreaturesEnteringAfterResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CivicWayfinder());
        harness.setHand(player1, List.of(new WojekSiren()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new CivicWayfinder());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lateCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Color sharing is checked against the target, not other boosted creatures")
    void doesNotSpreadBoostThroughMulticoloredCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CourierHawk());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        Permanent redCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        harness.setHand(player1, List.of(new WojekSiren()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, matchingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, matchingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, redCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, redCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boosts the target and every creature sharing a color with it")
    void boostsTargetAndColorSharingCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CivicWayfinder());
        Permanent ownMatchingCreature = harness.addToBattlefieldAndReturn(player1, new CivicWayfinder());
        Permanent opponentMatchingCreature = harness.addToBattlefieldAndReturn(player2, new CivicWayfinder());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        harness.setHand(player1, List.of(new WojekSiren()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownMatchingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownMatchingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentMatchingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentMatchingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, differentColorCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, differentColorCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("A colorless target does not boost other colorless creatures")
    void colorlessTargetOnlyBoostsItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GlassGolem());
        Permanent otherColorlessCreature = harness.addToBattlefieldAndReturn(player2, new GlassGolem());
        Permanent coloredCreature = harness.addToBattlefieldAndReturn(player2, new CivicWayfinder());
        harness.setHand(player1, List.of(new WojekSiren()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherColorlessCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, otherColorlessCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, coloredCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, coloredCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A multicolored target boosts creatures sharing either of its colors")
    void boostsCreaturesSharingEitherColorWithMulticoloredTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent redCreature = harness.addToBattlefieldAndReturn(player1, new ViashinoFangtail());
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player2, new CourierHawk());
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player2, new CivicWayfinder());
        harness.setHand(player1, List.of(new WojekSiren()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, redCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, redCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, whiteCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, greenCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Determines color-sharing creatures when the spell resolves")
    void determinesColorSharingCreaturesOnResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CivicWayfinder());
        harness.setHand(player1, List.of(new WojekSiren()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        Permanent creatureEnteringBeforeResolution =
                harness.addToBattlefieldAndReturn(player2, new CivicWayfinder());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, creatureEnteringBeforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creatureEnteringBeforeResolution)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CivicWayfinder());
        harness.setHand(player1, List.of(new WojekSiren()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does nothing if the target leaves before resolution")
    void doesNothingIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CivicWayfinder());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new CivicWayfinder());
        harness.setHand(player1, List.of(new WojekSiren()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, matchingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, matchingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new WojekSiren()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
