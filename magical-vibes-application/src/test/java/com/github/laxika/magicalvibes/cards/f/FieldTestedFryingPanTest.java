package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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

    private void castPan() {
        harness.setHand(player1, List.of(new FieldTestedFryingPan()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
