package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnmakeTheGraves.class, GrizzlyBears.class, LlanowarElves.class, LeoninScimitar.class, BlackCat.class})
class UnmakeTheGravesTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two chosen creature cards from the graveyard to hand")
    void returnsTwoChosenCreatures() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.setHand(player1, List.of(new UnmakeTheGraves()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0);

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        assertThat(validIds).hasSize(2);
        harness.handleMultipleCardsChosen(player1, validIds);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Unmake the Graves");
    }

    @Test
    @DisplayName("Up to two: choosing a single creature returns only that one")
    void choosingOneCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.setHand(player1, List.of(new UnmakeTheGraves()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0);

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, List.of(validIds.getFirst()));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Only creature cards are legal targets")
    void onlyCreatureCardsAreLegalTargets() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, new LeoninScimitar()));
        harness.setHand(player1, List.of(new UnmakeTheGraves()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Casting with no creature cards in the graveyard skips the prompt and resolves")
    void noCreaturesInGraveyard() {
        harness.setHand(player1, List.of(new UnmakeTheGraves()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Unmake the Graves");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void choosingZeroTargetsLeavesCreaturesInGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new UnmakeTheGraves()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Unmake the Graves");
    }

    @Test
    void opponentGraveyardCreaturesAreNotOffered() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new UnmakeTheGraves()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void returnsRemainingTargetWhenAnotherTargetLeavesGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new UnmakeTheGraves()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.setGraveyard(player1, List.of(second));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Unmake the Graves");
    }

    @Test
    void doesNotChooseReplacementTargetsWhenAllTargetsLeaveGraveyard() {
        Card target = new GrizzlyBears();
        Card unchosen = new LlanowarElves();
        harness.setGraveyard(player1, List.of(target, unchosen));
        harness.setHand(player1, List.of(new UnmakeTheGraves()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(unchosen));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Unmake the Graves");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickCreaturesCanConvokeGenericMana() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new UnmakeTheGraves()));
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elf.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(elf.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(elf.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        harness.assertInGraveyard(player1, "Unmake the Graves");
    }
    @Test
    void blackCreatureCanConvokeBlackMana() {
        Card target = new BlackCat();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new UnmakeTheGraves()));
        Permanent cat = harness.addToBattlefieldAndReturn(player1, new BlackCat());
        cat.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(cat.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(cat.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        harness.assertInGraveyard(player1, "Unmake the Graves");
    }
}
