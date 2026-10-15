package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BoneToAsh;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.FuneralCharm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChaosWand.class, Forest.class, Divination.class, ColossalDreadmaw.class,
        FuneralCharm.class, TormentingVoice.class, BoneToAsh.class})
class ChaosWandTest extends BaseCardTest {

    @Test
    @DisplayName("Offers the first instant or sorcery after exiling cards from an opponent's library")
    void offersFirstInstantOrSorcery() {
        activateWithLibrary(List.of(new Forest(), new Divination(), new ColossalDreadmaw()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Divination");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName).containsExactly("Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Casts the found spell for free under the activating player's control")
    void castsFoundSpellWithoutPaying() {
        activateWithLibrary(List.of(new Forest(), new Divination()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Divination")
                && entry.getEntryType() == StackEntryType.SORCERY_SPELL
                && entry.getControllerId().equals(player1.getId()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chooses a mode and target before resolving a modal found spell")
    void castsModalFoundSpellWithModeAndTarget() {
        activateWithLibrary(List.of(new Forest(), new FuneralCharm()));
        harness.setHand(player2, List.of(new Forest()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Target player discards a card");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Funeral Charm")
                && player2.getId().equals(entry.getTargetId()));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName).contains("Forest", "Funeral Charm");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).doesNotContain("Funeral Charm");
    }

    @Test
    @DisplayName("Bottoms the exiled cards when the activating player declines")
    void declineBottomsExiledCards() {
        activateWithLibrary(List.of(new Forest(), new Divination()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Divination"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName).containsExactlyInAnyOrder("Forest", "Divination");
    }

    @Test
    @DisplayName("Returns the entire library to the bottom when no instant or sorcery is found")
    void noInstantOrSorceryReturnsAllCards() {
        activateWithLibrary(List.of(new Forest(), new ColossalDreadmaw()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName).containsExactlyInAnyOrder("Forest", "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Cannot target the activating player")
    void cannotTargetSelf() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ChaosWand()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The found spell goes to its owner's graveyard after resolving")
    void resolvedSpellReturnsToOpponentsGraveyard() {
        Divination spell = new Divination();
        activateWithLibrary(List.of(spell));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    @DisplayName("A mandatory discard cost must be paid before the found spell is cast")
    void mandatoryDiscardCannotBeBypassed() {
        TormentingVoice spell = new TormentingVoice();
        Forest payment = new Forest();
        activateWithLibrary(List.of(spell));
        harness.setHand(player1, List.of(payment));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(spell.getId())
                && gd.playerHands.get(player1.getId()).contains(payment));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).contains(payment);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(payment);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(spell.getId())
                && entry.getOwnerId().equals(player2.getId()));
    }

    @Test
    @DisplayName("Cards removed from the opponent's library are in exile during the cast choice")
    void cardsActuallyEnterExile() {
        Forest skipped = new Forest();
        Divination spell = new Divination();
        activateWithLibrary(List.of(skipped, spell));

        harness.passBothPriorities();

        assertThat(gd.findExiledCard(skipped.getId())).isNotNull();
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    @DisplayName("An empty opposing library finishes without a cast choice")
    void emptyLibraryFinishes() {
        activateWithLibrary(List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An uncastable instant is bottomed without continuing to a later sorcery")
    void noLegalTargetBottomsFirstHit() {
        BoneToAsh spell = new BoneToAsh();
        Divination later = new Divination();
        Forest skipped = new Forest();
        activateWithLibrary(List.of(skipped, spell, later));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(later);
        assertThat(gd.playerDecks.get(player2.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skipped, spell);
    }

    private void activateWithLibrary(List<Card> library) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player2, library);
        harness.setHand(player1, List.of(new ChaosWand()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
    }
}
