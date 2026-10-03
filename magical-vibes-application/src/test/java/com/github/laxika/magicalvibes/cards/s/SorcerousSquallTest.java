package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SorcerousSquall.class, CounselOfTheSoratami.class, GrizzlyBears.class})
class SorcerousSquallTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent mills nine, then offers a spell already in that graveyard")
    void millsNineAndCastsExistingGraveyardSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player2, List.of(counsel));
        harness.setLibrary(player2, nineBears());
        harness.setHand(player1, List.of(new SorcerousSquall()));
        harness.addMana(player1, ManaColor.BLUE, 9);

        int handSizeBeforeCounsel = gd.playerHands.get(player1.getId()).size();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(counsel).hasSize(10);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size() - handSizeBeforeCounsel).isEqualTo(2);
        assertThat(gd.findExiledCard(counsel.getId())).isNotNull();
    }

    @Test
    @DisplayName("Does not offer a spell from the controller's graveyard")
    void onlySearchesTargetOpponentsGraveyard() {
        CounselOfTheSoratami ownCounsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(ownCounsel));
        harness.setLibrary(player2, nineBears());
        harness.setHand(player1, List.of(new SorcerousSquall()));
        harness.addMana(player1, ManaColor.BLUE, 9);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCounsel);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new SorcerousSquall()));
        harness.addMana(player1, ManaColor.BLUE, 9);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Card> nineBears() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }
}
