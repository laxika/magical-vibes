package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
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

@CardUsed({HonestRutstein.class, GrizzlyBears.class, HillGiant.class, HolyDay.class, LavaAxe.class})
class HonestRutsteinTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target creature card from its controller's graveyard to hand")
    void returnsTargetCreatureFromOwnGraveyard() {
        Card creature = new GrizzlyBears();
        Card nonCreature = new HolyDay();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonCreature, creature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        castHonestRutstein();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Holy Day");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does not create a choice without a creature in its controller's graveyard")
    void doesNotTargetNonCreatureOrOpponentGraveyard() {
        harness.setGraveyard(player1, List.of(new HolyDay()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        castHonestRutstein();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Holy Day");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creature spells you cast cost {1} less to cast")
    void reducesOwnCreatureSpellCost() {
        harness.addToBattlefield(player1, new HonestRutstein());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Hill Giant");
    }

    @Test
    @DisplayName("The reduction does not apply to noncreature spells")
    void doesNotReduceNonCreatureSpellCost() {
        harness.addToBattlefield(player1, new HonestRutstein());
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not apply to an opponent's creature spells")
    void doesNotReduceOpponentCreatureSpellCost() {
        harness.addToBattlefield(player1, new HonestRutstein());
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castHonestRutstein() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new HonestRutstein()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
