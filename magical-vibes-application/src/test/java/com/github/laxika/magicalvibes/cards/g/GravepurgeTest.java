package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GravepurgeTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting with creature cards in graveyard prompts for target selection")
    void castingWithCreaturesInGraveyardPromptsTargetSelection() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new GiantSpider();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.setHand(player1, List.of(new Gravepurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactlyInAnyOrder(creature1.getId(), creature2.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Selected creature card is put on top and drawn")
    void selectedCreatureCardIsDrawn() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Gravepurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);

        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().getName()).isEqualTo("Gravepurge");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Selecting zero targets still draws a card")
    void selectingZeroTargetsStillDraws() {
        Card graveyardCreature = new GrizzlyBears();
        Card topCard = new GiantSpider();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setHand(player1, List.of(new Gravepurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(graveyardCreature.getId()))
                .anyMatch(c -> c.getName().equals("Gravepurge"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("Only creature cards in your graveyard are valid targets")
    void onlyCreatureCardsInYourGraveyardAreValidTargets() {
        Card creature = new GrizzlyBears();
        Card nonCreature = new LightningBolt();
        Card opponentCreature = new GiantSpider();
        harness.setGraveyard(player1, List.of(creature, nonCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new Gravepurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Casting with no creature cards in graveyard skips target prompt and still draws")
    void castingWithNoCreaturesSkipsPromptAndDraws() {
        Card topCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new HolyDay()));
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setHand(player1, List.of(new Gravepurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Holy Day"))
                .anyMatch(c -> c.getName().equals("Gravepurge"));
    }
}
