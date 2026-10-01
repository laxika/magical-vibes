package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CatharticOperation.class, GrizzlyBears.class, MindStone.class, Cancel.class, Forest.class})
class CatharticOperationTest extends BaseCardTest {

    @Test
    void returnsUpToTwoCreatureCardsThenSeeksTwoNoncreatureNonlandCards() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new GrizzlyBears();
        Card soughtArtifact = new MindStone();
        Card soughtSpell = new Cancel();
        Card soughtLand = new Forest();
        Card soughtCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.setHand(player1, List.of(new CatharticOperation()));
        harness.setLibrary(player1, List.of(soughtLand, soughtCreature, soughtArtifact, soughtSpell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature1.getId(), creature2.getId());
        List<UUID> selected = List.of(creature1.getId(), creature2.getId());
        harness.handleMultipleCardsChosen(player1, selected);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(creature1, creature2, soughtArtifact, soughtSpell);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(soughtLand, soughtCreature);
    }
}
