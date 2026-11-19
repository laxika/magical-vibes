package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.g.GarruksPackleader;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.s.Shapesharer;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimalClay.class, Unsummon.class, Clone.class, GarruksPackleader.class, GrizzlyBears.class,
        Humility.class, Shapesharer.class})
class PrimalClayTest extends BaseCardTest {

    private Permanent castAndReturn(String chosenForm) {
        harness.castFromHand(player1, new PrimalClay(), new String(new char[]{'{', '4', '}'}));
        harness.passBothPriorities();
        if (chosenForm != null) {
            harness.handleListChoice(player1, chosenForm);
        }
        return findPermanent(player1, "Primal Clay");
    }

    @Test
    @DisplayName("Resolving awaits a shape choice")
    void resolvingAwaitsShapeChoice() {
        castAndReturn(null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing the 3/3 shape sets a 3/3 with no flying or defender")
    void threeThreeShape() {
        Permanent clay = castAndReturn("THREE_THREE");

        assertThat(gqs.getEffectivePower(gd, clay)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, clay)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, clay, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, clay, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, clay, CardSubtype.WALL)).isFalse();
    }

    @Test
    @DisplayName("Choosing the 2/2 flying shape sets a 2/2 with flying")
    void twoTwoFlyingShape() {
        Permanent clay = castAndReturn("TWO_TWO_FLYING");

        assertThat(gqs.getEffectivePower(gd, clay)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, clay)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, clay, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, clay, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, clay, CardSubtype.WALL)).isFalse();
    }

    @Test
    @DisplayName("The chosen Wall type survives an end-of-turn reset")
    void chosenWallSubtypePersistsAcrossTurns() {
        Permanent clay = castAndReturn(com.github.laxika.magicalvibes.model.PrimalClayForm.ONE_SIX_WALL.name());

        clay.resetModifiers();

        assertThat(gqs.hasEffectiveSubtype(gd, clay, CardSubtype.WALL)).isTrue();
    }
    @DisplayName("Choosing the 1/6 Wall shape sets a 1/6 Wall with defender")
    @Test
    void oneSixWallShape() {
        Permanent clay = castAndReturn("ONE_SIX_WALL");

        assertThat(gqs.getEffectivePower(gd, clay)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, clay)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, clay, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, clay, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, clay, CardSubtype.WALL)).isTrue();
    }

    @Test
    @DisplayName("The chosen shape's P/T and keyword survive an end-of-turn reset")
    void chosenShapePersistsAcrossTurns() {
        Permanent clay = castAndReturn("TWO_TWO_FLYING");

        // Model the cleanup-step reset of until-end-of-turn modifiers; the chosen shape is a
        // permanent characteristic and must remain (unlike transient grantedKeywords).
        clay.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, clay)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, clay)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, clay, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting it again after returning to hand asks for a new shape")
    void returningToHandAsksForNewShape() {
        Permanent clay = castAndReturn("TWO_TWO_FLYING");

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, clay.getId());

        harness.assertNotOnBattlefield(player1, "Primal Clay");
        harness.assertInHand(player1, "Primal Clay");

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "ONE_SIX_WALL");

        Permanent returnedClay = findPermanent(player1, "Primal Clay");
        assertThat(gqs.getEffectivePower(gd, returnedClay)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returnedClay)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, returnedClay, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, returnedClay, CardSubtype.WALL)).isTrue();
    }

    @Test
    @DisplayName("The 1/6 Wall shape has defender and can't attack")
    void wallShapeCannotAttack() {
        Permanent clay = castAndReturn(com.github.laxika.magicalvibes.model.PrimalClayForm.ONE_SIX_WALL.name());
        clay.setSummoningSick(false);

        assertThat(als.canAttack(gd, clay, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("The 3/3 shape is set before enter triggers check its power")
    void threeThreeShapeTriggersPackleaderAfterChoice() {
        harness.addToBattlefield(player1, new GarruksPackleader());
        harness.setLibrary(player1, List.of(new Unsummon()));

        castAndReturn("THREE_THREE");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Unsummon");
    }

    @Test
    @DisplayName("The 2/2 flying shape does not trigger an enter ability that needs power 3 or greater")
    void flyingShapeDoesNotTriggerPackleader() {
        harness.addToBattlefield(player1, new GarruksPackleader());

        castAndReturn("TWO_TWO_FLYING");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A Clone of the flying shape keeps flying and takes the P/T of its own 3/3 choice")
    void cloneRetainsCopiedFlyingWhenChoosingThreeThreeForm() {
        Permanent clay = castAndReturn("TWO_TWO_FLYING");
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, clay.getId());
        harness.handleListChoice(player1, "THREE_THREE");

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(clay.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, copy, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A Clone of the Wall shape keeps defender and the Wall type and takes the P/T of its own flying choice")
    void cloneRetainsCopiedWallAndDefenderWhenChoosingFlyingForm() {
        Permanent clay = castAndReturn("ONE_SIX_WALL");
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, clay.getId());
        harness.handleListChoice(player1, "TWO_TWO_FLYING");

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(clay.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, copy, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, copy, CardSubtype.WALL)).isTrue();
    }

    @Test
    @DisplayName("The chosen shape is a copiable value, so an earlier Humility still makes it a 1/1 with no abilities")
    void earlierHumilityOverridesTheChosenShape() {
        harness.addToBattlefield(player2, new Humility());

        Permanent clay = castAndReturn("TWO_TWO_FLYING");

        assertThat(gqs.getEffectivePower(gd, clay)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, clay)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, clay, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Becoming a copy of another creature replaces the chosen shape's P/T, keyword and Wall type")
    void becomingACopyReplacesTheChosenShape() {
        Permanent clay = castAndReturn("ONE_SIX_WALL");
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new Shapesharer());
        harness.addMana(player1, ManaColor.BLUE, 3);

        int shapesharerIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
        harness.activateAbilityWithMultiTargets(player1, shapesharerIndex, 0, List.of(clay.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, clay)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, clay)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, clay, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, clay, CardSubtype.WALL)).isFalse();
    }

    @Test
    @DisplayName("A permanent that becomes a copy of it takes the chosen shape")
    void becomingACopyOfItTakesTheChosenShape() {
        Permanent clay = castAndReturn("TWO_TWO_FLYING");
        Permanent shapesharer = addCreatureReady(player1, new Shapesharer());
        harness.addMana(player1, ManaColor.BLUE, 3);

        int shapesharerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(shapesharer);
        harness.activateAbilityWithMultiTargets(player1, shapesharerIndex, 0,
                List.of(shapesharer.getId(), clay.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shapesharer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shapesharer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, shapesharer, Keyword.FLYING)).isTrue();
    }
}
