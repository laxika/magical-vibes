package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.i.InBolassClutches;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({SylvanAwakening.class, Forest.class, Mountain.class, LlanowarElves.class, InBolassClutches.class, Humility.class})
class SylvanAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sylvan Awakening animates all lands you control as 2/2 Elemental creatures")
    void animatesAllLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new SylvanAwakening(), "{2}{G}");
        harness.passBothPriorities();

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest).isNotNull();
        assertThat(gd.animatedByFloatingEffect(forest.getId())).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveSubtypes(gd, forest)).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();

        Permanent mountain = findPermanent(player1, "Mountain");
        assertThat(mountain).isNotNull();
        assertThat(gd.animatedByFloatingEffect(mountain.getId())).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(2);
    }

    @Test
    @DisplayName("Animated after Humility, the lands keep the animation's reach, indestructible, haste and 2/2 (CR 613.7)")
    void animationAfterHumilityKeepsItsAbilities() {
        harness.addToBattlefield(player2, new Humility());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new SylvanAwakening(), "{2}{G}");
        harness.passBothPriorities();

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
    }

    @Test
    @DisplayName("Animated lands are treated as creatures")
    void animatedLandsAreCreatures() {
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new SylvanAwakening(), "{2}{G}");
        harness.passBothPriorities();

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest).isNotNull();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Animated lands have reach, indestructible, and haste keywords")
    void animatedLandsHaveKeywords() {
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new SylvanAwakening(), "{2}{G}");
        harness.passBothPriorities();

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest).isNotNull();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not animate non-land permanents")
    void doesNotAnimateNonLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new SylvanAwakening(), "{2}{G}");
        harness.passBothPriorities();

        Permanent elves = findPermanent(player1, "Llanowar Elves");
        assertThat(elves).isNotNull();
        assertThat(gd.animatedByFloatingEffect(elves.getId())).isFalse();
    }

    @Test
    @DisplayName("Does not animate opponent's lands")
    void doesNotAnimateOpponentLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new SylvanAwakening(), "{2}{G}");
        harness.passBothPriorities();

        Permanent opponentForest = findPermanent(player2, "Forest");
        assertThat(opponentForest).isNotNull();
        assertThat(gd.animatedByFloatingEffect(opponentForest.getId())).isFalse();
    }

    @Test
    @DisplayName("Animation survives through the opponent's turn")
    void animationSurvivesEndOfTurn() {
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new SylvanAwakening(), "{2}{G}");
        harness.passBothPriorities();

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest).isNotNull();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        // Animation should still be present
        assertThat(gd.animatedByFloatingEffect(forest.getId())).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectiveSubtypes(gd, forest)).contains(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Animation is cleared at beginning of the caster's next turn")
    void animationClearedAtNextTurn() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SylvanAwakening(), "{2}{G}");
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, forest)).isTrue();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.REACH)).isFalse();
        assertThat(gqs.getEffectiveSubtypes(gd, forest)).doesNotContain(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Animated lands are still lands (retain land type)")
    void animatedLandsAreStillLands() {
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new SylvanAwakening(), "{2}{G}");
        harness.passBothPriorities();

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest).isNotNull();
        assertThat(gqs.isLand(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Animated lands have the Elemental subtype in effective creature queries")
    void animatedLandsAreElementals() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SylvanAwakening(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, forest)).contains(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Lands entering after resolution are not animated, and tapped lands remain tapped")
    void affectsOnlyLandsPresentAtResolutionWithoutUntapping() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SylvanAwakening(), "{2}{G}");
        harness.passBothPriorities();
        Permanent mountain = harness.enterBattlefieldAndReturn(player1, new Mountain());

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(forest.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, mountain)).isFalse();
    }

    @Test
    @DisplayName("A stolen animated land loses its animation at the caster's next turn")
    void stolenLandAnimationExpiresAtCastersNextTurn() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SylvanAwakening(), "{2}{G}");
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new InBolassClutches()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castEnchantment(player2, 0, forest.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest);

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isFalse();
    }
}
