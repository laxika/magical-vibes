package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AttuneWithAether;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KujarSeedsculptor;
import com.github.laxika.magicalvibes.cards.r.RiparianTiger;
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

@CardUsed({WildestDreams.class, KujarSeedsculptor.class, RiparianTiger.class,
        AttuneWithAether.class, Forest.class})
class WildestDreamsTest extends BaseCardTest {

    private void addManaForXTwo() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Test
    @DisplayName("Returns exactly X target cards from your graveyard to your hand")
    void returnsExactlyXCardsFromOwnGraveyard() {
        Card seedsculptor = new KujarSeedsculptor();
        Card tiger = new RiparianTiger();
        Card opponentCard = new KujarSeedsculptor();
        harness.setGraveyard(player1, List.of(seedsculptor, tiger));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new WildestDreams()));
        addManaForXTwo();

        harness.castSorcery(player1, 0, 2);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(seedsculptor.getId(), tiger.getId());
        harness.handleMultipleCardsChosen(player1, List.of(seedsculptor.getId(), tiger.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Kujar Seedsculptor");
        harness.assertInHand(player1, "Riparian Tiger");
        harness.assertInGraveyard(player2, "Kujar Seedsculptor");
    }

    @Test
    @DisplayName("Wildest Dreams is exiled after resolving")
    void isExiledAfterResolution() {
        WildestDreams dreams = new WildestDreams();
        harness.setHand(player1, List.of(dreams));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dreams);
        harness.assertNotInGraveyard(player1, "Wildest Dreams");
    }

    @Test
    @DisplayName("Must choose exactly X graveyard targets")
    void mustChooseExactlyXTargets() {
        Card seedsculptor = new KujarSeedsculptor();
        harness.setGraveyard(player1, List.of(seedsculptor));
        harness.setHand(player1, List.of(new WildestDreams()));
        addManaForXTwo();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=0 resolves without returning cards")
    void xZeroReturnsNoCards() {
        Card seedsculptor = new KujarSeedsculptor();
        harness.setGraveyard(player1, List.of(seedsculptor));
        harness.setHand(player1, List.of(new WildestDreams()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Kujar Seedsculptor");
    }

    @Test
    @DisplayName("Returns noncreature cards, including lands, and leaves unchosen cards")
    void returnsNoncreatureCards() {
        Card land = new Forest();
        Card sorcery = new AttuneWithAether();
        Card unchosen = new KujarSeedsculptor();
        WildestDreams dreams = new WildestDreams();
        harness.setGraveyard(player1, List.of(land, sorcery, unchosen));
        harness.setHand(player1, List.of(dreams));
        addManaForXTwo();

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(land.getId(), sorcery.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(land, sorcery);
        harness.assertInGraveyard(player1, "Kujar Seedsculptor");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dreams);
    }

    @Test
    @DisplayName("Returns remaining legal targets and exiles itself when one target leaves")
    void resolvesWithOneTargetRemaining() {
        Card remaining = new KujarSeedsculptor();
        Card removed = new RiparianTiger();
        WildestDreams dreams = new WildestDreams();
        harness.setGraveyard(player1, List.of(remaining, removed));
        harness.setHand(player1, List.of(dreams));
        addManaForXTwo();

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(remaining.getId(), removed.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        harness.setExile(player1, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(removed, dreams);
        harness.assertNotInGraveyard(player1, "Wildest Dreams");
    }

    @Test
    @DisplayName("Goes to the graveyard instead of exile when all targets become illegal")
    void doesNotResolveWhenAllTargetsLeave() {
        Card target = new KujarSeedsculptor();
        WildestDreams dreams = new WildestDreams();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(dreams));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wildest Dreams");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target).doesNotContain(dreams);
        harness.assertNotInHand(player1, "Kujar Seedsculptor");
    }

    @Test
    @DisplayName("Rejects too few targets, duplicate targets, and an opponent's graveyard card")
    void rejectsInvalidSelections() {
        Card first = new KujarSeedsculptor();
        Card second = new RiparianTiger();
        Card opponentCard = new Forest();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new WildestDreams()));
        addManaForXTwo();

        harness.castSorcery(player1, 0, 2);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId(), opponentCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("X=2 requires four generic mana in addition to green")
    void requiresPaymentForBothXSymbols() {
        harness.setGraveyard(player1, List.of(new KujarSeedsculptor(), new RiparianTiger()));
        harness.setHand(player1, List.of(new WildestDreams()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Wildest Dreams");
        assertThat(gd.stack).isEmpty();
    }
}
