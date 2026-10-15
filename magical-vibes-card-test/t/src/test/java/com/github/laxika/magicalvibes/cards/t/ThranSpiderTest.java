package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThranSpider.class, Ornithopter.class, Shock.class, GrizzlyBears.class, Plains.class})
class ThranSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a tapped Powerstone for each player")
    void etbCreatesPowerstonesForBothPlayers() {
        harness.setHand(player1, List.of(new ThranSpider()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(powerstones(player1)).hasSize(1);
        assertThat(powerstones(player2)).hasSize(1);
        assertThat(powerstones(player1).getFirst().isTapped()).isTrue();
        assertThat(powerstones(player2).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activated ability offers only an artifact from the top four")
    void activatedAbilityOffersArtifactFromTopFour() {
        addCreatureReady(player1, new ThranSpider());
        Card artifact = new Ornithopter();
        harness.setLibrary(player1, List.of(artifact, new Shock(), new GrizzlyBears(), new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Activated ability may decline the artifact reveal")
    void activatedAbilityMayDecline() {
        addCreatureReady(player1, new ThranSpider());
        Card artifact = new Ornithopter();
        harness.setLibrary(player1, List.of(artifact, new Shock(), new GrizzlyBears(), new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private List<Permanent> powerstones(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.POWERSTONE))
                .toList();
    }

    @Test
    @DisplayName("Only one artifact from the top four is taken, and deeper cards stay on top")
    void choosesOneArtifactAndPreservesUnseenCards() {
        addCreatureReady(player1, new ThranSpider());
        Card first = new ThranSpider();
        Card second = new ThranSpider();
        Card third = new Plains();
        Card fourth = new Plains();
        Card unseen = new ThranSpider();
        harness.setLibrary(player1, List.of(first, second, third, fourth, unseen));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(first, unseen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(unseen, first, third, fourth);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unseen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A short library still allows declining its only artifact")
    void shortLibraryMayDeclineOnlyArtifact() {
        addCreatureReady(player1, new ThranSpider());
        Card artifact = new ThranSpider();
        harness.setLibrary(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Without an artifact the looked-at cards go beneath the untouched library")
    void noArtifactMovesLookedAtCardsToBottom() {
        addCreatureReady(player1, new ThranSpider());
        Card first = new Plains();
        Card second = new Plains();
        Card third = new Plains();
        Card fourth = new Plains();
        Card unseen = new ThranSpider();
        harness.setLibrary(player1, List.of(first, second, third, fourth, unseen));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second, third, fourth, unseen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(unseen, first, second, third, fourth);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unseen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a draw")
    void emptyLibraryResolvesWithoutChoice() {
        addCreatureReady(player1, new ThranSpider());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
