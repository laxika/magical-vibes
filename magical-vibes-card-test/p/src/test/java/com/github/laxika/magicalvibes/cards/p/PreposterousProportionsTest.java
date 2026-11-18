package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BearCub;
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

@CardUsed({PreposterousProportions.class, BearCub.class})
class PreposterousProportionsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Preposterous Proportions gives your creatures +10/+10 and vigilance")
    void boostsAndGrantsVigilanceToOwnCreatures() {
        harness.addToBattlefield(player1, new BearCub());
        harness.addToBattlefield(player2, new BearCub());
        harness.setHand(player1, List.of(new PreposterousProportions()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent ownCreature = findPermanent(player1, "Bear Cub");
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(12);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();

        Permanent opposingCreature = findPermanent(player2, "Bear Cub");
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The boost and vigilance wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new BearCub());
        harness.setHand(player1, List.of(new PreposterousProportions()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent creature = findPermanent(player1, "Bear Cub");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(12);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creatures present at resolution are affected, but later arrivals are not")
    void affectsOnlyCreaturesPresentAtResolution() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new BearCub());
        harness.setHand(player1, List.of(new PreposterousProportions()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castSorcery(player1, 0, 0);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new BearCub());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new BearCub());

        for (Permanent affected : List.of(original, beforeResolution)) {
            assertThat(gqs.getEffectivePower(gd, affected)).isEqualTo(12);
            assertThat(gqs.getEffectiveToughness(gd, affected)).isEqualTo(12);
            assertThat(gqs.hasKeyword(gd, affected, Keyword.VIGILANCE)).isTrue();
        }
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The spell resolves with no creatures and does not affect later arrivals")
    void resolvesWithNoCreatures() {
        harness.setHand(player1, List.of(new PreposterousProportions()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Preposterous Proportions");
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new BearCub());
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Granted vigilance lets a boosted creature attack without tapping")
    void grantedVigilancePreventsTappingToAttack() {
        Permanent creature = addCreatureReady(player1, new BearCub());
        harness.setHand(player1, List.of(new PreposterousProportions()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castAndResolveSorcery(player1, 0, 0);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.isAttacking()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(12);
    }
}
