package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PullFromTheGrave.class, GrizzlyBears.class, LlanowarElves.class, LeoninScimitar.class})
class PullFromTheGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two targeted creatures to hand and controller gains 2 life")
    void returnsTwoCreaturesAndGainsLife() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.setHand(player1, List.of(new PullFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, 0);

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Controller gains 2 life even when selecting zero graveyard targets")
    void gainsLifeWithZeroTargets() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new PullFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Controller gains 2 life when graveyard has no creature cards")
    void gainsLifeWithNoCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new PullFromTheGrave(), "{2}{B}");
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Leonin Scimitar");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Only creature cards are valid graveyard targets")
    void onlyCreatureCardsAreValidTargets() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new LlanowarElves();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature1, creature2, artifact));
        harness.setHand(player1, List.of(new PullFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactlyInAnyOrder(creature1.getId(), creature2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Returns one selected creature and leaves the other in the graveyard")
    void returnsOneSelectedCreature() {
        Card selected = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(selected, new LlanowarElves()));
        harness.setHand(player1, List.of(new PullFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotInHand(player1, "Llanowar Elves");
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("Opponent's creature cards are not offered as targets")
    void excludesOpponentsGraveyard() {
        Card ownCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new PullFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertNotInHand(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Returns the remaining target and gains life if one target leaves the graveyard")
    void resolvesWithOneRemainingTarget() {
        Card remaining = new GrizzlyBears();
        Card removed = new LlanowarElves();
        harness.setGraveyard(player1, List.of(remaining, removed));
        harness.setHand(player1, List.of(new PullFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(remaining.getId(), removed.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        harness.setExile(player1, List.of(removed));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Llanowar Elves");
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("Does not gain life when all selected targets leave the graveyard")
    void doesNotResolveWithAllTargetsIllegal() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new PullFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Pull from the Grave");
        harness.assertLife(player1, lifeBefore);
    }
}
