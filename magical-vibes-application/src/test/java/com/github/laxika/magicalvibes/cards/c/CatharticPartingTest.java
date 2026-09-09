package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CatharticParting.class, GloriousAnthem.class, LeoninScimitar.class, GrizzlyBears.class, HolyDay.class})
class CatharticPartingTest extends BaseCardTest {

    @Test
    void shufflesAnOpponentsArtifactAndUpToFourCardsFromYourGraveyard() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        List<Card> graveyard = List.of(
                new GrizzlyBears(), new HolyDay(), new GrizzlyBears(), new HolyDay(), new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new CatharticParting()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, artifact.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyElementsOf(
                graveyard.stream().map(Card::getId).toList());
        assertThat(choice.maxCount()).isEqualTo(4);

        List<Card> shuffledCards = graveyard.subList(0, 4);
        harness.handleMultipleCardsChosen(player1, shuffledCards.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(artifact.getId()));
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId).contains(artifact.getCard().getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId).containsAll(
                        shuffledCards.stream().map(Card::getId).toList());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).contains(graveyard.get(4).getId());
    }

    @Test
    void mayShuffleNoGraveyardCardsAndStillShufflesAnEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new CatharticParting()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, enchantment.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(enchantment.getId()));
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId).contains(enchantment.getCard().getId());
    }

    @Test
    void canTargetOnlyAnArtifactOrEnchantmentAnOpponentControls() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CatharticParting()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, ownArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }
}
