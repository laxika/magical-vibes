package com.github.laxika.magicalvibes.cards.p;

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

@CardUsed({PhyrexianMissionary.class, GrizzlyBears.class, CruelEdict.class})
class PhyrexianMissionaryTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotReturnCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new PhyrexianMissionary()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phyrexian Missionary");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void kickedReturnsTargetCreatureFromYourGraveyardToHand() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new PhyrexianMissionary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phyrexian Missionary");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void kickedCannotReturnNonCreatureCard() {
        Card spell = new CruelEdict();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(new PhyrexianMissionary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
        harness.assertOnBattlefield(player1, "Phyrexian Missionary");
        harness.assertInGraveyard(player1, "Cruel Edict");
    }

    @Test
    void kickedWithEmptyGraveyardStillEnters() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new PhyrexianMissionary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phyrexian Missionary");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kickedCannotReturnCreatureFromOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new PhyrexianMissionary()));
        harness.setHand(player1, List.of(new PhyrexianMissionary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phyrexian Missionary");
        harness.assertInGraveyard(player2, "Phyrexian Missionary");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lifelinkGainsLifeFromCombatDamage() {
        addCreatureReady(player1, new PhyrexianMissionary());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void kickedReturnsOnlyTheSelectedCreature() {
        Card chosen = new PhyrexianMissionary();
        Card other = new PhyrexianMissionary();
        harness.setGraveyard(player1, List.of(chosen, other));
        harness.setHand(player1, List.of(new PhyrexianMissionary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen).doesNotContain(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other).doesNotContain(chosen);
    }

    @Test
    void removedTargetDoesNotReturnAnotherCreature() {
        Card chosen = new PhyrexianMissionary();
        Card other = new PhyrexianMissionary();
        harness.setGraveyard(player1, List.of(chosen, other));
        harness.setHand(player1, List.of(new PhyrexianMissionary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(chosen));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(chosen, other);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
