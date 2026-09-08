package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormidableSpeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB may discard a card and offers creature cards")
    void acceptingEtbMayDiscardAndSearchesForCreature() {
        GrizzlyBears discard = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new FormidableSpeaker(), discard)));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.getGameData().playerDecks.get(player1.getId()).clear();
        harness.getGameData().playerDecks.get(player1.getId()).addAll(List.of(new GrizzlyBears(), new Island()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice discardChoice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discardChoice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards())
                .hasSize(1)
                .allMatch(card -> card.hasType(CardType.CREATURE));

        harness.getGameService().handleInteractionAnswer(
                harness.getGameData(), player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the ETB may does not discard or search")
    void decliningEtbMayDoesNothing() {
        GrizzlyBears cardInHand = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new FormidableSpeaker(), cardInHand)));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .containsExactly(cardInHand);
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Activated ability untaps another target permanent")
    void activatedAbilityUntapsAnotherPermanent() {
        Permanent speaker = addReadySpeaker();
        Permanent target = addTappedPermanent();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(speaker.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activated ability cannot target Formidable Speaker itself")
    void activatedAbilityCannotTargetItself() {
        Permanent speaker = addReadySpeaker();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, speaker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySpeaker() {
        Permanent speaker = new Permanent(new FormidableSpeaker());
        speaker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(speaker);
        return speaker;
    }

    private Permanent addTappedPermanent() {
        Permanent target = new Permanent(new GrizzlyBears());
        target.tap();
        gd.playerBattlefields.get(player1.getId()).add(target);
        return target;
    }
}
