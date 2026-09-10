package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodBeckoning.class, GrizzlyBears.class, LlanowarElves.class, LeoninScimitar.class})
class BloodBeckoningTest extends BaseCardTest {

    @Test
    void returnsOneTargetWithoutKicker() {
        Card creature = new GrizzlyBears();
        Card otherCreature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature, otherCreature));
        harness.setHand(player1, List.of(new BloodBeckoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(otherCreature.getId());
    }

    @Test
    void returnsTwoTargetsWhenKicked() {
        Card creature = new GrizzlyBears();
        Card otherCreature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature, otherCreature));
        harness.setHand(player1, List.of(new BloodBeckoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedSorcery(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), otherCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(creature.getId(), otherCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void onlyCreatureCardsAreValidTargets() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new BloodBeckoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
    }

    @Test
    void kickedCastRequiresTwoCreatureCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BloodBeckoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castKickedSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
