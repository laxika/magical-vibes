package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChakraMeditation.class, AirbendingLesson.class, Forest.class, GrizzlyBears.class,
        HolyDay.class, Shock.class})
class ChakraMeditationTest extends BaseCardTest {

    @Test
    void returnsOptionalTargetInstantOrSorceryFromGraveyard() {
        Card instant = new HolyDay();
        ChakraMeditation meditation = new ChakraMeditation();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(meditation));
        addManaForMeditation();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(instant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Holy Day");
        harness.assertOnBattlefield(player1, "Chakra Meditation");
    }

    @Test
    void canResolveWithoutChoosingGraveyardTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ChakraMeditation()));
        addManaForMeditation();

        harness.castEnchantment(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chakra Meditation");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void drawsAndDiscardsWhenFewerThanThreeLessonsAreInGraveyard() {
        harness.setGraveyard(player1, List.of(new AirbendingLesson(), new AirbendingLesson()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new ChakraMeditation(), new GrizzlyBears(), new Shock()));
        harness.addToBattlefield(player1, new ChakraMeditation());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void doesNotDiscardWhenThreeLessonsAreInGraveyard() {
        harness.setGraveyard(player1, List.of(
                new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new ChakraMeditation(), new Shock()));
        harness.addToBattlefield(player1, new ChakraMeditation());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Chakra Meditation");
    }

    @Test
    void cannotTargetCreatureCardInGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ChakraMeditation()));
        addManaForMeditation();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addManaForMeditation() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
