package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.c.CivicWayfinder;
import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
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

@CardUsed({SurgeOfZeal.class, BorosRecruit.class, BorosSignet.class, CivicWayfinder.class,
        CourierHawk.class, GlassGolem.class, ViashinoFangtail.class})
class SurgeOfZealTest extends BaseCardTest {

    @Test
    @DisplayName("Gives haste to the target and every creature sharing a color with it")
    void grantsHasteToTargetAndColorSharingCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CivicWayfinder());
        Permanent ownMatchingCreature = harness.addToBattlefieldAndReturn(player1, new CivicWayfinder());
        Permanent opponentMatchingCreature = harness.addToBattlefieldAndReturn(player2, new CivicWayfinder());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        harness.setHand(player1, List.of(new SurgeOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownMatchingCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentMatchingCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, differentColorCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A colorless target affects only itself")
    void colorlessTargetOnlyAffectsItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GlassGolem());
        Permanent otherColorlessCreature = harness.addToBattlefieldAndReturn(player2, new GlassGolem());
        Permanent coloredCreature = harness.addToBattlefieldAndReturn(player2, new CivicWayfinder());
        harness.setHand(player1, List.of(new SurgeOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherColorlessCreature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, coloredCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A multicolored target affects creatures sharing either color")
    void multicoloredTargetAffectsCreaturesSharingEitherColor() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent redCreature = harness.addToBattlefieldAndReturn(player1, new ViashinoFangtail());
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player2, new CourierHawk());
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player2, new CivicWayfinder());
        harness.setHand(player1, List.of(new SurgeOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, redCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, whiteCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, greenCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Determines color-sharing creatures when the spell resolves")
    void determinesColorSharingCreaturesOnResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CivicWayfinder());
        harness.setHand(player1, List.of(new SurgeOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        Permanent creatureEnteringBeforeResolution =
                harness.addToBattlefieldAndReturn(player2, new CivicWayfinder());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creatureEnteringBeforeResolution, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Granted haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CivicWayfinder());
        harness.setHand(player1, List.of(new SurgeOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does nothing if the target leaves before resolution")
    void doesNothingIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CivicWayfinder());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new CivicWayfinder());
        harness.setHand(player1, List.of(new SurgeOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, matchingCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain haste")
    void creaturesEnteringAfterResolutionDoNotGainHaste() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CivicWayfinder());
        harness.setHand(player1, List.of(new SurgeOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new CivicWayfinder());

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Color sharing is determined only from the target, including an opposing target")
    void colorSharingDoesNotSpreadThroughOtherAffectedCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        Permanent multicoloredCreature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player2, new CourierHawk());
        harness.setHand(player1, List.of(new SurgeOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, multicoloredCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, whiteCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreature() {
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new BorosSignet());
        harness.setHand(player1, List.of(new SurgeOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, signet.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
