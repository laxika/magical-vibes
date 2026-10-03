package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YourMistakeIsMyTriumph.class, GrizzlyBears.class, Shock.class})
class YourMistakeIsMyTriumphTest extends BaseCardTest {

    @Test
    void millsEachPlayerAndPutsAChosenPermanentOntoBattlefieldUnderController() {
        Card ownNonPermanent = new Shock();
        Card opponentPermanent = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Shock(), ownNonPermanent, new Shock()));
        harness.setLibrary(player2, List.of(new Shock(), opponentPermanent, new Shock()));

        resolveScheme();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownNonPermanent);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentPermanent);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(opponentPermanent.getId());

        harness.handleMultipleCardsChosen(player1, List.of(opponentPermanent.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentPermanent);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == opponentPermanent);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() == opponentPermanent);
    }

    @Test
    void doesNotOfferNonPermanentMilledCards() {
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock()));

        resolveScheme();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private void resolveScheme() {
        Card scheme = new YourMistakeIsMyTriumph();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL),
                null,
                (Zone) null));
        harness.passBothPriorities();
    }
}
