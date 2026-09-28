package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JungleWeaver;
import com.github.laxika.magicalvibes.cards.y.YokedPlowbeast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeraldOfTheForgotten.class, YokedPlowbeast.class, JungleWeaver.class,
        GrizzlyBears.class, Censor.class})
class HeraldOfTheForgottenTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Herald returns any number of target permanent cards with cycling")
    void castingReturnsSelectedCyclingPermanents() {
        Card first = new YokedPlowbeast();
        Card second = new JungleWeaver();
        Card nonPermanent = new Censor();
        Card nonCycling = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second, nonPermanent, nonCycling));
        harness.setHand(player1, List.of(new HeraldOfTheForgotten()));
        addHeraldMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Yoked Plowbeast");
        harness.assertOnBattlefield(player1, "Jungle Weaver");
        harness.assertInGraveyard(player1, "Censor");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("No valid cycling permanent cards means no graveyard choice")
    void noValidCardsMeansNoChoice() {
        harness.setGraveyard(player1, List.of(new Censor(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new HeraldOfTheForgotten()));
        addHeraldMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Censor");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void addHeraldMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
