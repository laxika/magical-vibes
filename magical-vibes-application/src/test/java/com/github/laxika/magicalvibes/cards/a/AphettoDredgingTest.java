package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AphettoDredging.class, GoblinPiker.class, GrizzlyBears.class,
        LeoninScimitar.class, WoodlandChangeling.class})
class AphettoDredgingTest extends BaseCardTest {

    @Test
    void choosesUpToThreeCreaturesOfTheChosenType() {
        Card goblin = new GoblinPiker();
        Card bear = new GrizzlyBears();
        Card changeling = new WoodlandChangeling();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(goblin, bear, changeling, artifact));
        harness.setHand(player1, List.of(new AphettoDredging()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithChosenCreatureType(player1, 0, 0, CardSubtype.GOBLIN, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(goblin.getId(), changeling.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(goblin.getId(), changeling.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Goblin Piker");
        harness.assertInHand(player1, "Woodland Changeling");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    void noMatchingCardsPutsTheSpellOnTheStackWithoutAPrompt() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new AphettoDredging()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithChosenCreatureType(player1, 0, 0, CardSubtype.GOBLIN, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
