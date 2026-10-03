package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HamletCaptain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DecimatorOfTheProvinces.class, HamletCaptain.class})
class DecimatorOfTheProvincesTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: when cast, own creatures get +2/+2 and trample until end of turn")
    void hardcastBoostsOwnCreatures() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HamletCaptain());
        harness.setHand(player1, List.of(new DecimatorOfTheProvinces()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve cast trigger
        harness.passBothPriorities(); // resolve creature spell

        assertThat(ally.getEffectivePower()).isEqualTo(4);
        assertThat(ally.getEffectiveToughness()).isEqualTo(4);
        assertThat(ally.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.assertOnBattlefield(player1, "Decimator of the Provinces");
    }

    @Test
    @DisplayName("Emerge: sacrifice a creature, pay emerge cost reduced by its mana value")
    void emergeSacrificesAndReducesCost() {
        harness.addToBattlefield(player1, new HamletCaptain()); // MV 2
        UUID bearsId = harness.getPermanentId(player1, "Hamlet Captain");
        Permanent other = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());

        harness.setHand(player1, List.of(new DecimatorOfTheProvinces()));
        // Emerge {6}{G}{G}{G} reduced by 2 → {4}{G}{G}{G}
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(other.getEffectivePower()).isEqualTo(4);
        assertThat(other.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Decimator of the Provinces"))
                .noneMatch(p -> p.getId().equals(bearsId));
        harness.assertInGraveyard(player1, "Hamlet Captain");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Emerge fails without enough mana after reduction")
    void emergeFailsWithInsufficientMana() {
        harness.addToBattlefield(player1, new HamletCaptain()); // MV 2
        UUID bearsId = harness.getPermanentId(player1, "Hamlet Captain");

        harness.setHand(player1, List.of(new DecimatorOfTheProvinces()));
        // Need {4}{G}{G}{G} after reduction; only {3}{G}{G}{G} available
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cast trigger resolves before the creature spell")
    void castTriggerResolvesBeforeCreature() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());
        harness.setHand(player1, List.of(new DecimatorOfTheProvinces()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve trigger only

        assertThat(ally.getEffectivePower()).isEqualTo(4);
        assertThat(ally.hasKeyword(Keyword.TRAMPLE)).isTrue();
        harness.assertNotOnBattlefield(player1, "Decimator of the Provinces");
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Decimator of the Provinces");
    }

    @Test
    @DisplayName("Cast trigger boosts wear off at end of turn")
    void boostsWearOffAtEndOfTurn() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());
        harness.setHand(player1, List.of(new DecimatorOfTheProvinces()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ally.getEffectivePower()).isEqualTo(4);
        assertThat(ally.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ally.getEffectivePower()).isEqualTo(2);
        assertThat(ally.getEffectiveToughness()).isEqualTo(2);
        assertThat(ally.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cast trigger affects creatures present on resolution, but not later arrivals")
    void castTriggerLocksInCreaturesOnResolution() {
        harness.setHand(player1, List.of(new DecimatorOfTheProvinces()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.castCreature(player1, 0);

        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());
        harness.passBothPriorities();

        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());
        harness.passBothPriorities();

        assertThat(beforeResolution.getEffectivePower()).isEqualTo(4);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(4);
        assertThat(beforeResolution.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(afterResolution.getEffectivePower()).isEqualTo(2);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(2);
        assertThat(afterResolution.hasKeyword(Keyword.TRAMPLE)).isFalse();
        Permanent decimator = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof DecimatorOfTheProvinces)
                .findFirst().orElseThrow();
        assertThat(decimator.getPowerModifier()).isZero();
        assertThat(decimator.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Entering without being cast does not boost other creatures")
    void enteringWithoutCastingDoesNotTrigger() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());

        harness.enterBattlefieldAndReturn(player1, new DecimatorOfTheProvinces());

        assertThat(gd.stack).isEmpty();
        assertThat(ally.getEffectivePower()).isEqualTo(2);
        assertThat(ally.getEffectiveToughness()).isEqualTo(2);
        assertThat(ally.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Emerge reduction above the generic cost still requires three green mana")
    void emergeReductionStopsAtColoredCost() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DecimatorOfTheProvinces());
        harness.setHand(player1, List.of(new DecimatorOfTheProvinces()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrifice.getId()));

        harness.assertInGraveyard(player1, "Decimator of the Provinces");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Decimator of the Provinces");
    }

    @Test
    @DisplayName("Emerge cannot replace a required green mana with colorless mana")
    void emergeCannotReduceColoredMana() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DecimatorOfTheProvinces());
        harness.setHand(player1, List.of(new DecimatorOfTheProvinces()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrifice.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
