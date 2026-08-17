package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GruesomeMenagerieTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one creature card with each of mana values one, two, and three")
    void returnsOneCreatureOfEachManaValue() {
        Card oneManaCreature = new ElvishMystic();
        Card twoManaCreature = new GrizzlyBears();
        Card threeManaCreature = new CentaurCourser();
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(oneManaCreature, twoManaCreature, threeManaCreature, nonCreature));
        castAndResolve();

        harness.assertOnBattlefield(player1, "Elvish Mystic");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Centaur Courser");
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    @DisplayName("Requires a choice and does not allow declining when multiple cards share a mana value")
    void requiresChoiceForMultipleMatchingCards() {
        Card oneManaCreature = new ElvishMystic();
        Card firstTwoManaCreature = new GrizzlyBears();
        Card secondTwoManaCreature = new GrizzlyBears();
        Card threeManaCreature = new CentaurCourser();
        harness.setGraveyard(player1, List.of(oneManaCreature, firstTwoManaCreature, secondTwoManaCreature, threeManaCreature));
        castAndResolve();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.mandatory()).isTrue();
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot decline forced graveyard choice");

        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Elvish Mystic");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Centaur Courser");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstTwoManaCreature)
                .doesNotContain(secondTwoManaCreature);
    }

    @Test
    @DisplayName("Skips missing mana values and still returns later matches")
    void skipsMissingManaValues() {
        Card oneManaCreature = new ElvishMystic();
        Card threeManaCreature = new CentaurCourser();
        harness.setGraveyard(player1, List.of(oneManaCreature, threeManaCreature));
        castAndResolve();

        harness.assertOnBattlefield(player1, "Elvish Mystic");
        harness.assertOnBattlefield(player1, "Centaur Courser");
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new GruesomeMenagerie()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
