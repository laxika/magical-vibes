package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.h.HiredPoisoner;
import com.github.laxika.magicalvibes.cards.o.OrneryGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GruesomeMenagerie.class, ElvishMystic.class, GrizzlyBears.class, CentaurCourser.class,
        HolyDay.class, HiredPoisoner.class, OrneryGoblin.class})
class GruesomeMenagerieTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one creature card with each of mana values one, two, and three")
    @CardUsed({GruesomeMenagerie.class, ElvishMystic.class, GrizzlyBears.class, CentaurCourser.class, HolyDay.class})
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
    @CardUsed({GruesomeMenagerie.class, ElvishMystic.class, GrizzlyBears.class, CentaurCourser.class})
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
    @CardUsed({GruesomeMenagerie.class, ElvishMystic.class, CentaurCourser.class})
    void skipsMissingManaValues() {
        Card oneManaCreature = new ElvishMystic();
        Card threeManaCreature = new CentaurCourser();
        harness.setGraveyard(player1, List.of(oneManaCreature, threeManaCreature));
        castAndResolve();

        harness.assertOnBattlefield(player1, "Elvish Mystic");
        harness.assertOnBattlefield(player1, "Centaur Courser");
    }

    @Test
    @DisplayName("Keeps all creatures in the graveyard until every return choice is complete")
    @CardUsed({GruesomeMenagerie.class, HiredPoisoner.class, OrneryGoblin.class})
    void waitsForAllChoicesBeforeReturningCreatures() {
        Card oneManaCreature = new HiredPoisoner();
        Card firstTwoManaCreature = new OrneryGoblin();
        Card secondTwoManaCreature = new OrneryGoblin();
        harness.setGraveyard(player1, List.of(oneManaCreature, firstTwoManaCreature, secondTwoManaCreature));
        castAndResolve();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.assertNotOnBattlefield(player1, "Hired Poisoner");
        harness.assertNotOnBattlefield(player1, "Ornery Goblin");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(oneManaCreature, firstTwoManaCreature, secondTwoManaCreature);

        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Hired Poisoner");
        harness.assertOnBattlefield(player1, "Ornery Goblin");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(secondTwoManaCreature)
                .doesNotContain(oneManaCreature, firstTwoManaCreature);
    }

    @Test
    @DisplayName("Resolves with an empty graveyard without returning an opponent's creatures")
    @CardUsed({GruesomeMenagerie.class, HiredPoisoner.class, OrneryGoblin.class})
    void ignoresOpponentsGraveyard() {
        Card opponentsOneManaCreature = new HiredPoisoner();
        Card opponentsTwoManaCreature = new OrneryGoblin();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentsOneManaCreature, opponentsTwoManaCreature));
        castAndResolve();

        harness.assertNotOnBattlefield(player1, "Hired Poisoner");
        harness.assertNotOnBattlefield(player1, "Ornery Goblin");
        harness.assertNotOnBattlefield(player2, "Hired Poisoner");
        harness.assertNotOnBattlefield(player2, "Ornery Goblin");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(opponentsOneManaCreature, opponentsTwoManaCreature);
        harness.assertInGraveyard(player1, "Gruesome Menagerie");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new GruesomeMenagerie()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
