package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FieldTestedFryingPan.class)
class FieldTestedFryingPanTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Food token and attaches to a Halfling token")
    void entersWithFoodAndAttachedHalfling() {
        castPan();

        Permanent pan = findPermanent(player1, "Field-Tested Frying Pan");
        Permanent food = findPermanent(player1, "Food");
        Permanent halfling = findPermanent(player1, "Halfling");

        assertThat(food.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(food.getCard().getSubtypes()).contains(CardSubtype.FOOD);
        assertThat(halfling.getCard().getPower()).isEqualTo(1);
        assertThat(halfling.getCard().getToughness()).isEqualTo(1);
        assertThat(halfling.getCard().getSubtypes()).contains(CardSubtype.HALFLING);
        assertThat(pan.getAttachedTo()).isEqualTo(halfling.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +X/+X for life gained")
    void lifeGainBoostsEquippedCreature() {
        castPan();

        Permanent halfling = findPermanent(player1, "Halfling");
        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.activateAbility(player1, foodIndex, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gqs.getEffectivePower(gd, halfling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, halfling)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip moves the granted ability to the new creature")
    void equipMovesLifeGainAbility() {
        castPan();
        Permanent firstHalfling = findPermanent(player1, "Halfling");
        Permanent firstPan = findPermanent(player1, "Field-Tested Frying Pan");
        castPan();
        Permanent secondHalfling = findPermanents(player1, "Halfling").get(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(firstPan),
                0, null, secondHalfling.getId());
        harness.passBothPriorities();
        assertThat(firstPan.getAttachedTo()).isEqualTo(secondHalfling.getId());

        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food),
                0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstHalfling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, firstHalfling)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, secondHalfling)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, secondHalfling)).isEqualTo(7);
    }

    @Test
    @DisplayName("Separate life gains accumulate and the boosts expire at end of turn")
    void separateLifeGainsAccumulateUntilEndOfTurn() {
        castPan();
        castPan();
        List<Permanent> halflings = findPermanents(player1, "Halfling");
        List<Permanent> foods = findPermanents(player1, "Food");

        for (Permanent food : foods) {
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food),
                    0, null, null);
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        assertThat(gd.getLife(player1.getId())).isEqualTo(26);
        for (Permanent halfling : halflings) {
            assertThat(gqs.getEffectivePower(gd, halfling)).isEqualTo(7);
            assertThat(gqs.getEffectiveToughness(gd, halfling)).isEqualTo(7);
        }

        harness.passUntil(player2, TurnStep.UPKEEP);

        for (Permanent halfling : halflings) {
            assertThat(gqs.getEffectivePower(gd, halfling)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, halfling)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Opponent life gain does not boost your equipped creature")
    void opponentLifeGainDoesNotTrigger() {
        castPan();
        Permanent halfling = findPermanent(player1, "Halfling");
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new FieldTestedFryingPan()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent food = findPermanent(player2, "Food");
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(food),
                0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(23);
        assertThat(gqs.getEffectivePower(gd, halfling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, halfling)).isEqualTo(1);
        Permanent opponentHalfling = findPermanent(player2, "Halfling");
        assertThat(gqs.getEffectivePower(gd, opponentHalfling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentHalfling)).isEqualTo(4);
    }
    @Test
    @DisplayName("A life gain in response to equip boosts the original creature")
    void lifeGainInResponseToEquipBoostsOriginalCreature() {
        castPan();
        Permanent firstPan = findPermanent(player1, "Field-Tested Frying Pan");
        Permanent firstHalfling = findPermanent(player1, "Halfling");
        castPan();
        Permanent secondHalfling = findPermanents(player1, "Halfling").get(1);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(firstPan),
                0, null, secondHalfling.getId());
        Permanent food = findPermanent(player1, "Food");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food),
                0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstPan.getAttachedTo()).isEqualTo(secondHalfling.getId());
        assertThat(gqs.getEffectivePower(gd, firstHalfling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstHalfling)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondHalfling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondHalfling)).isEqualTo(4);
    }
    private void castPan() {
        harness.setHand(player1, List.of(new FieldTestedFryingPan()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
