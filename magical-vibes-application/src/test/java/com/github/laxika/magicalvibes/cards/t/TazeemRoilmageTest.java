package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TazeemRoilmage.class, LightningBolt.class, CruelEdict.class, GrizzlyBears.class})
class TazeemRoilmageTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotReturnASpell() {
        Card spell = new LightningBolt();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(new TazeemRoilmage()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tazeem Roilmage");
        harness.assertInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    void kickedReturnsAnInstantOrSorceryFromTheGraveyard() {
        Card instant = new LightningBolt();
        Card sorcery = new CruelEdict();
        harness.setGraveyard(player1, List.of(instant, sorcery));
        harness.setHand(player1, List.of(new TazeemRoilmage()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(instant.getId(), sorcery.getId());

        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Cruel Edict");
        harness.assertNotInGraveyard(player1, "Cruel Edict");
        harness.assertInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    void kickedCannotReturnANonSpellCard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new TazeemRoilmage()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Tazeem Roilmage");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
