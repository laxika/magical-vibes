package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.cards.d.DimirInfiltrator;
import com.github.laxika.magicalvibes.cards.j.Junktroller;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.cards.v.ViashinoSlasher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RallyTheRighteous.class, BorosRecruit.class, DimirInfiltrator.class,
        ViashinoSlasher.class, Junktroller.class, CourierHawk.class, LastGasp.class})
class RallyTheRighteousTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps and boosts the target and every creature sharing a color with it")
    void untapsAndBoostsTargetAndColorSharingCreatures() {
        Permanent target = addTappedCreature(player1, new ViashinoSlasher());
        Permanent ownMatchingCreature = addTappedCreature(player1, new BorosRecruit());
        Permanent opponentMatchingCreature = addTappedCreature(player2, new BorosRecruit());
        Permanent differentColorCreature = addTappedCreature(player2, new DimirInfiltrator());

        castRally(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(ownMatchingCreature.isTapped()).isFalse();
        assertThat(opponentMatchingCreature.isTapped()).isFalse();
        assertThat(differentColorCreature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownMatchingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentMatchingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, differentColorCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("A colorless target does not affect other colorless creatures")
    void colorlessTargetOnlyAffectsItself() {
        Permanent target = addTappedCreature(player1, new Junktroller());
        Permanent otherColorlessCreature = addTappedCreature(player2, new Junktroller());
        Permanent coloredCreature = addTappedCreature(player2, new BorosRecruit());

        castRally(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(otherColorlessCreature.isTapped()).isTrue();
        assertThat(coloredCreature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherColorlessCreature)).isZero();
        assertThat(gqs.getEffectivePower(gd, coloredCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("The power boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addTappedCreature(player1, new ViashinoSlasher());
        Permanent matchingCreature = addTappedCreature(player2, new BorosRecruit());

        castRally(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, matchingCreature)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, matchingCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("A multicolor target affects either color and boosts each creature only once")
    void multicolorTargetAffectsEitherColorOnlyOnce() {
        Permanent target = addTappedCreature(player2, new BorosRecruit());
        Permanent bothColors = addTappedCreature(player1, new BorosRecruit());
        Permanent redCreature = addTappedCreature(player1, new ViashinoSlasher());
        Permanent whiteCreature = addTappedCreature(player2, new CourierHawk());
        Permanent unrelatedCreature = addTappedCreature(player2, new DimirInfiltrator());

        castRally(target);

        for (Permanent affected : List.of(target, bothColors, redCreature, whiteCreature)) {
            assertThat(affected.isTapped()).isFalse();
            assertThat(gqs.getEffectivePower(gd, affected)).isEqualTo(3);
        }
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, whiteCreature)).isEqualTo(2);
        assertThat(unrelatedCreature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, unrelatedCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Already untapped creatures still receive the boost")
    void alreadyUntappedCreaturesReceiveBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ViashinoSlasher());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());

        castRally(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(matchingCreature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, matchingCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void laterCreaturesDoNotReceiveBoost() {
        Permanent target = addTappedCreature(player1, new ViashinoSlasher());

        castRally(target);
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("An illegal target prevents both the untap and the boost")
    void removedTargetPreventsAllEffects() {
        Permanent target = addTappedCreature(player1, new ViashinoSlasher());
        Permanent matchingCreature = addTappedCreature(player2, new BorosRecruit());
        harness.setHand(player1, List.of(new RallyTheRighteous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new LastGasp()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInGraveyard(player1, "Viashino Slasher");
        harness.passBothPriorities();

        assertThat(matchingCreature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, matchingCreature)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Rally the Righteous");
    }

    private void castRally(Permanent target) {
        harness.setHand(player1, List.of(new RallyTheRighteous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private Permanent addTappedCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.tap();
        return permanent;
    }
}
