package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeafLeapGuide.class, GrizzlyBears.class, TurnToFrog.class})
class LeafLeapGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Perpetually boosts the Guide and entering creature")
    void perpetuallyBoostsTheGuideAndEnteringCreature() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new LeafLeapGuide());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guide)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(3);
    }

    @Test
    @DisplayName("Perpetual boost survives returning creature to hand")
    void perpetualBoostSurvivesReturningCreatureToHand() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new LeafLeapGuide());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        guide.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, entering.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");

        Card enteringCard = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        harness.castFromHand(player1, enteringCard, "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).singleElement()
                .satisfies(reentered -> {
                    assertThat(gqs.getEffectivePower(gd, reentered)).isEqualTo(4);
                    assertThat(gqs.getEffectiveToughness(gd, reentered)).isEqualTo(4);
                });
    }

    @Test
    @DisplayName("Cannot return the Guide itself")
    void cannotReturnTheGuideItself() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new LeafLeapGuide());
        guide.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, guide.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not trigger for itself or an opponent's creature")
    void doesNotTriggerForItselfOrAnOpponentsCreature() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new LeafLeapGuide());
        assertThat(gd.stack).isEmpty();
        Permanent opponent = harness.enterBattlefieldAndReturn(player2, new LeafLeapGuide());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot return an opponent's creature")
    void cannotReturnAnOpponentsCreature() {
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new LeafLeapGuide());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new LeafLeapGuide());
        guide.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each Guide boosts itself and the entering creature")
    void eachGuideBoostsItselfAndTheEnteringCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LeafLeapGuide());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LeafLeapGuide());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new LeafLeapGuide());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(3);
    }

    @Test
    @DisplayName("The perpetual boost stays when the creature loses all abilities")
    void perpetualBoostIsNotAnAbilityThatCanBeLost() {
        harness.enterBattlefieldAndReturn(player1, new LeafLeapGuide());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, entering.getId());

        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(2);
    }
}
