package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.b.BowOfNylea;
import com.github.laxika.magicalvibes.cards.c.CampusGuide;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
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

@CardUsed({ReconstructHistory.class, AjaniGoldmane.class, DarksteelRelic.class,
        GrizzlyBears.class, HolyDay.class, HonorOfThePure.class, LavaAxe.class,
        BowOfNylea.class, CampusGuide.class})
class ReconstructHistoryTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to one card of each listed type from the graveyard")
    void returnsUpToOneCardOfEachListedType() {
        Card artifact = new DarksteelRelic();
        Card enchantment = new HonorOfThePure();
        Card instant = new HolyDay();
        Card sorcery = new LavaAxe();
        Card planeswalker = new AjaniGoldmane();
        Card spell = new ReconstructHistory();
        harness.setGraveyard(player1, List.of(artifact, enchantment, instant, sorcery, planeswalker));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(5);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                artifact.getId(), enchantment.getId(), instant.getId(), sorcery.getId(), planeswalker.getId());

        harness.handleMultipleCardsChosen(player1, List.of(
                artifact.getId(), enchantment.getId(), instant.getId(), sorcery.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(
                        artifact.getId(), enchantment.getId(), instant.getId(), sorcery.getId(), planeswalker.getId());
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .contains(spell.getId());
    }

    @Test
    @DisplayName("Does not allow two cards of the same listed type")
    void doesNotAllowTwoCardsOfTheSameListedType() {
        Card firstInstant = new HolyDay();
        Card secondInstant = new HolyDay();
        Card sorcery = new LavaAxe();
        Card spell = new ReconstructHistory();
        harness.setGraveyard(player1, List.of(firstInstant, secondInstant, sorcery));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstInstant.getId(), secondInstant.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one instant card");

        harness.handleMultipleCardsChosen(player1, List.of(firstInstant.getId(), sorcery.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(firstInstant.getId(), sorcery.getId());
    }

    @Test
    @DisplayName("Exiles itself when no listed card types are in the graveyard")
    void exilesItselfWithNoEligibleCards() {
        Card creature = new GrizzlyBears();
        Card spell = new ReconstructHistory();
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, spell, "{2}{R}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .contains(spell.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getId());
    }

    @Test
    @DisplayName("An artifact enchantment can be the enchantment target alongside another artifact")
    void returnsArtifactEnchantmentAlongsideArtifact() {
        Card artifact = new CampusGuide();
        Card enchantment = new BowOfNylea();
        Card spell = new ReconstructHistory();
        harness.setGraveyard(player1, List.of(artifact, enchantment));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId(), enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(artifact.getId(), enchantment.getId());
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).contains(spell.getId());
    }

    @Test
    @DisplayName("May choose zero targets even when an eligible card exists")
    void mayChooseZeroTargets() {
        Card artifact = new CampusGuide();
        Card spell = new ReconstructHistory();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(artifact.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).contains(spell.getId());
    }

    @Test
    @DisplayName("Does not resolve or exile itself if its only target leaves the graveyard")
    void doesNotExileItselfWhenAllTargetsBecomeIllegal() {
        Card artifact = new CampusGuide();
        Card spell = new ReconstructHistory();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(artifact));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(spell.getId());
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).doesNotContain(spell.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(artifact.getId());
    }

    @Test
    @DisplayName("Returns remaining legal targets and exiles itself if another target leaves")
    void returnsRemainingLegalTargets() {
        Card artifact = new CampusGuide();
        Card sorcery = new ReconstructHistory();
        Card spell = new ReconstructHistory();
        harness.setGraveyard(player1, List.of(artifact, sorcery));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId(), sorcery.getId()));
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(artifact));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(artifact.getId(), sorcery.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).contains(spell.getId());
    }
}
