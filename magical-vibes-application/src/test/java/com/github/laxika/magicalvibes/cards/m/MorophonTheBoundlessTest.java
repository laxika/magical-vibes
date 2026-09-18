package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MorophonTheBoundless.class, GrizzlyBears.class})
class MorophonTheBoundlessTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type as Morophon enters sets the chosen subtype")
    void choosingCreatureTypeSetsChosenSubtype() {
        harness.setHand(player1, List.of(new MorophonTheBoundless()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(findPermanent(player1, "Morophon, the Boundless").getChosenSubtype())
                .isEqualTo(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Other creatures you control of the chosen type get +1/+1")
    void boostsOtherOwnCreaturesOfChosenType() {
        addMorophon(CardSubtype.BEAR);
        harness.addToBattlefield(player1, new GrizzlyBears());

        var bonus = gqs.computeStaticBonus(gd, findPermanent(player1, "Grizzly Bears"));

        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Morophon does not boost itself")
    void doesNotBoostItself() {
        Permanent morophon = addMorophon(CardSubtype.BEAR);

        var bonus = gqs.computeStaticBonus(gd, morophon);

        assertThat(bonus.power()).isZero();
        assertThat(bonus.toughness()).isZero();
    }

    @Test
    @DisplayName("Chosen-type spells lose the five colored mana symbols from their cost")
    void reducesColoredManaForChosenTypeSpells() {
        addMorophon(CardSubtype.BEAR);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Morophon's colored reduction does not pay generic mana")
    void coloredReductionDoesNotPayGenericMana() {
        addMorophon(CardSubtype.BEAR);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Colored mana can pay the generic cost remaining after Morophon's reduction")
    void coloredManaPaysRemainingGenericCost() {
        addMorophon(CardSubtype.BEAR);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The boost does not affect an opponent's creature")
    void doesNotBoostOpponentCreature() {
        addMorophon(CardSubtype.BEAR);
        harness.addToBattlefield(player2, new GrizzlyBears());

        var bonus = gqs.computeStaticBonus(gd, findPermanent(player2, "Grizzly Bears"));

        assertThat(bonus.power()).isZero();
        assertThat(bonus.toughness()).isZero();
    }

    private Permanent addMorophon(CardSubtype chosenSubtype) {
        Permanent morophon = harness.addToBattlefieldAndReturn(player1, new MorophonTheBoundless());
        morophon.setChosenSubtype(chosenSubtype);
        return morophon;
    }
}
