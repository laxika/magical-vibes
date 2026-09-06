package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathOfGhouls.class, Forest.class, GrizzlyBears.class})
class OathOfGhoulsTest extends BaseCardTest {

    @Test
    void activePlayerChoosesOpponentWithFewerCreatureCardsAndReturnsTheirOwnCreature() {
        Card returned = new GrizzlyBears();
        harness.addToBattlefield(player1, new OathOfGhouls());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(returned));

        advanceToUpkeep(player2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player1.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleGraveyardCardChosen(player2, 0);

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    void decliningLeavesActivePlayersGraveyardUntouched() {
        harness.addToBattlefield(player1, new OathOfGhouls());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .doesNotContain("Grizzly Bears");
    }

    @Test
    void targetMustStillHaveFewerCreatureCardsWhenTheAbilityResolves() {
        harness.addToBattlefield(player1, new OathOfGhouls());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .doesNotContain("Grizzly Bears");
    }

    @Test
    void doesNotTriggerWhenNoOpponentHasFewerCreatureCards() {
        harness.addToBattlefield(player1, new OathOfGhouls());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
