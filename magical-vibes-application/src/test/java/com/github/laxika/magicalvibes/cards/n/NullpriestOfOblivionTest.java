package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NullpriestOfOblivion.class, GrizzlyBears.class, CruelEdict.class})
class NullpriestOfOblivionTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotReturnCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new NullpriestOfOblivion()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nullpriest of Oblivion");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void kickedReturnsTargetCreatureFromYourGraveyardToTheBattlefield() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new NullpriestOfOblivion()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nullpriest of Oblivion");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void kickedCannotReturnNonCreatureCard() {
        Card spell = new CruelEdict();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(new NullpriestOfOblivion()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
        harness.assertOnBattlefield(player1, "Nullpriest of Oblivion");
        harness.assertInGraveyard(player1, "Cruel Edict");
    }

    @Test
    void kickedWithEmptyGraveyardStillEnters() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new NullpriestOfOblivion()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nullpriest of Oblivion");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kickedCannotReturnCreatureFromOpponentsGraveyard() {
        Card creature = new NullpriestOfOblivion();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new NullpriestOfOblivion()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nullpriest of Oblivion");
        harness.assertInGraveyard(player2, "Nullpriest of Oblivion");
        harness.assertNotOnBattlefield(player2, "Nullpriest of Oblivion");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kickedReturnsOnlyChosenCreatureAndReturnedCreatureIsNotKicked() {
        Card chosen = new NullpriestOfOblivion();
        Card other = new NullpriestOfOblivion();
        harness.setGraveyard(player1, List.of(chosen, other));
        harness.setHand(player1, List.of(new NullpriestOfOblivion()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(chosen.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(other.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetLeavingGraveyardDoesNotReturnAnotherCreatureInstead() {
        Card target = new NullpriestOfOblivion();
        Card other = new NullpriestOfOblivion();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(new NullpriestOfOblivion()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(other.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
