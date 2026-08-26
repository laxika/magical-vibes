package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReenactTheCrime.class, Forest.class, GrizzlyBears.class, TormentingVoice.class})
class ReenactTheCrimeTest extends BaseCardTest {

    @Test
    @DisplayName("Only a nonland card put into a graveyard this turn can be targeted")
    void onlyCardPutIntoGraveyardThisTurnCanBeTargeted() {
        GrizzlyBears oldGraveyardCard = new GrizzlyBears();
        GrizzlyBears discardedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldGraveyardCard));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TormentingVoice(), discardedCard, new ReenactTheCrime()));
        addManaForVoiceAndReenact();

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, oldGraveyardCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.castInstant(player1, 0, discardedCard.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Exiles the card and offers a free copy cast")
    void exilesCardAndCastsCopyForFree() {
        GrizzlyBears discardedCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TormentingVoice(), discardedCard, new ReenactTheCrime()));
        addManaForVoiceAndReenact();

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, discardedCard.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(discardedCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
    }

    private void addManaForVoiceAndReenact() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
