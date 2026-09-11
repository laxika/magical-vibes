package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerfolkFalconer.class, AcademyDrake.class, Forest.class, GrizzlyBears.class})
class MerfolkFalconerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a kicked spell triggers scry 2")
    void kickedSpellTriggersScryTwo() {
        harness.addToBattlefield(player1, new MerfolkFalconer());
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        Card cardToBottom = scry.cards().getFirst();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isNotSameAs(cardToBottom);
        assertThat(gd.playerDecks.get(player1.getId()).get(gd.playerDecks.get(player1.getId()).size() - 1))
                .isSameAs(cardToBottom);
    }

    @Test
    @DisplayName("Casting a non-kicked spell does not trigger scry")
    void nonKickedSpellDoesNotTriggerScry() {
        harness.addToBattlefield(player1, new MerfolkFalconer());
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }
}
