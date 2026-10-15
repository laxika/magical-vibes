package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrowthSpiral;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.cards.s.ScrabblingClaws;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Regenesis.class, SauroformHybrid.class, ScrabblingClaws.class, GrowthSpiral.class})
class RegenesisTest extends BaseCardTest {

    @Test
    void returnsTwoTargetPermanentCardsToHand() {
        Card creature = new SauroformHybrid();
        Card artifact = new ScrabblingClaws();
        Card instant = new GrowthSpiral();
        Card spell = new Regenesis();
        harness.setGraveyard(player1, List.of(creature, artifact, instant));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castInstant(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), artifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(creature.getId(), artifact.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(instant.getId(), spell.getId());
    }

    @Test
    void canReturnOnlyOneTarget() {
        Card permanent = new SauroformHybrid();
        Card otherPermanent = new ScrabblingClaws();
        Card spell = new Regenesis();
        harness.setGraveyard(player1, List.of(permanent, otherPermanent));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(permanent.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(otherPermanent.getId(), spell.getId());
    }

    @Test
    void excludesNonPermanentCards() {
        Card instant = new GrowthSpiral();
        Card spell = new Regenesis();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(instant.getId(), spell.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void canChooseZeroTargetsEvenWhenPermanentsAreAvailable() {
        Card permanent = new SauroformHybrid();
        Card spell = new Regenesis();
        harness.setGraveyard(player1, List.of(permanent));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(permanent.getId(), spell.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsRemainingTargetWhenOneTargetLeavesGraveyard() {
        Card creature = new SauroformHybrid();
        Card artifact = new ScrabblingClaws();
        Card spell = new Regenesis();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), artifact.getId()));
        harness.setGraveyard(player1, List.of(artifact));
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId).containsExactly(artifact.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).containsExactly(spell.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotChoosePermanentFromOpponentsGraveyard() {
        Card ownPermanent = new SauroformHybrid();
        Card opposingPermanent = new ScrabblingClaws();
        harness.setGraveyard(player1, List.of(ownPermanent));
        harness.setGraveyard(player2, List.of(opposingPermanent));
        harness.setHand(player1, List.of(new Regenesis()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castInstant(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownPermanent.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownPermanent.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId).containsExactly(ownPermanent.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId).containsExactly(opposingPermanent.getId());
    }
}
