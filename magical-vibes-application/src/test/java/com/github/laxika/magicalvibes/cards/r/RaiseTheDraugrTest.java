package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BergStrider;
import com.github.laxika.magicalvibes.cards.b.BrinebarrowIntruder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.m.Mistwalker;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaiseTheDraugr.class, GrizzlyBears.class, YoungWolf.class,
        BergStrider.class, BrinebarrowIntruder.class, MaskwoodNexus.class, Mistwalker.class})
class RaiseTheDraugrTest extends BaseCardTest {

    @Test
    @DisplayName("The single-card mode returns a creature card from the graveyard to hand")
    void returnsOneCreatureCard() {
        Card creature = new GrizzlyBears();
        Card spell = new RaiseTheDraugr();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Raise the Draugr");
    }

    @Test
    @DisplayName("The two-card mode returns two creature cards that share a creature type")
    void returnsTwoCreaturesSharingType() {
        Card firstBear = new GrizzlyBears();
        Card secondBear = new GrizzlyBears();
        Card wolf = new YoungWolf();
        Card spell = new RaiseTheDraugr();
        harness.setGraveyard(player1, List.of(firstBear, secondBear, wolf));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstant(player1, 0, 1, List.of());

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactly(firstBear.getId(), secondBear.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBear.getId(), secondBear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(firstBear.getId(), secondBear.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(wolf.getId());
        harness.assertInGraveyard(player1, "Raise the Draugr");
    }

    @Test
    @DisplayName("The shared-type mode cannot be cast without a legal creature pair")
    void requiresASharedTypePair() {
        Card bear = new GrizzlyBears();
        Card wolf = new YoungWolf();
        Card spell = new RaiseTheDraugr();
        harness.setGraveyard(player1, List.of(bear, wolf));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("The shared-type mode recognizes creature types granted by Maskwood Nexus")
    void returnsDifferentPrintedTypesWithMaskwoodNexus() {
        Card giant = new BergStrider();
        Card rogue = new BrinebarrowIntruder();
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.setGraveyard(player1, List.of(giant, rogue));
        harness.setHand(player1, List.of(new RaiseTheDraugr()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId(), rogue.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(giant.getId(), rogue.getId());
        harness.assertInGraveyard(player1, "Raise the Draugr");
    }

    @Test
    @DisplayName("Changeling shares a creature type with a creature card in the graveyard")
    void returnsChangelingAndGiant() {
        Card changeling = new Mistwalker();
        Card giant = new BergStrider();
        harness.setGraveyard(player1, List.of(changeling, giant));
        harness.setHand(player1, List.of(new RaiseTheDraugr()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(changeling.getId(), giant.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(changeling.getId(), giant.getId());
        harness.assertInGraveyard(player1, "Raise the Draugr");
    }

    @Test
    @DisplayName("The remaining target returns when the other shared-type target leaves the graveyard")
    void returnsRemainingTarget() {
        Card first = new BergStrider();
        Card second = new BergStrider();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RaiseTheDraugr()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.setGraveyard(player1, List.of(second));
        harness.setExile(player1, List.of(first));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(second.getId());
        harness.assertInGraveyard(player1, "Raise the Draugr");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The two-card mode rejects selecting only one creature")
    void cannotSelectOnlyOneTargetForSecondMode() {
        Card first = new BergStrider();
        Card second = new BergStrider();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RaiseTheDraugr()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstant(player1, 0, 1, List.of());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }
    @Test
    @DisplayName("Eligible individual cards still must form a shared-type pair")
    void rejectsUnrelatedPairDespiteEachSharingWithChangeling() {
        Card giant = new BergStrider();
        Card rogue = new BrinebarrowIntruder();
        Card changeling = new Mistwalker();
        harness.setGraveyard(player1, List.of(giant, rogue, changeling));
        harness.setHand(player1, List.of(new RaiseTheDraugr()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstant(player1, 0, 1, List.of());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(giant.getId(), rogue.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId(), changeling.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(giant.getId(), changeling.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(rogue.getId());
    }

    @Test
    @DisplayName("The single-card mode offers only creatures in your own graveyard")
    void excludesNoncreaturesAndOpponentsCards() {
        Card creature = new BergStrider();
        Card artifact = new MaskwoodNexus();
        Card opposingCreature = new BrinebarrowIntruder();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setHand(player1, List.of(new RaiseTheDraugr()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(artifact.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
    }

    @Test
    @DisplayName("The single-card mode cannot be cast with no creature in your graveyard")
    void requiresOneCreatureInOwnGraveyard() {
        Card spell = new RaiseTheDraugr();
        harness.setGraveyard(player1, List.of(new MaskwoodNexus()));
        harness.setGraveyard(player2, List.of(new BergStrider()));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
    }
}
