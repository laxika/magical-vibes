package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SkitterOfLizards;
import com.github.laxika.magicalvibes.cards.t.TazeemRoilmage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MurasaSproutling.class, TazeemRoilmage.class, SkitterOfLizards.class, LightningBolt.class})
class MurasaSproutlingTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotReturnACard() {
        Card kickerCard = new TazeemRoilmage();
        harness.setGraveyard(player1, List.of(kickerCard));
        harness.setHand(player1, List.of(new MurasaSproutling()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Murasa Sproutling");
        harness.assertInGraveyard(player1, "Tazeem Roilmage");
    }

    @Test
    void kickedReturnsTargetCardWithKickerFromTheGraveyard() {
        Card kickerCard = new TazeemRoilmage();
        Card multikickerCard = new SkitterOfLizards();
        Card nonKickerCard = new LightningBolt();
        harness.setGraveyard(player1, List.of(kickerCard, multikickerCard, nonKickerCard));
        harness.setHand(player1, List.of(new MurasaSproutling()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(kickerCard.getId(), multikickerCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(kickerCard.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Tazeem Roilmage");
        harness.assertInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    void kickedDoesNotCreateAChoiceWhenNoCardHasKicker() {
        Card nonKickerCard = new LightningBolt();
        harness.setGraveyard(player1, List.of(nonKickerCard));
        harness.setHand(player1, List.of(new MurasaSproutling()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertOnBattlefield(player1, "Murasa Sproutling");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
