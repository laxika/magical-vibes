package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AngelicBlessing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathOfScholars.class, AngelicBlessing.class})
class OathOfScholarsTest extends BaseCardTest {

    @Test
    void activePlayerMayDiscardTheirHandAndDrawThreeCards() {
        harness.addToBattlefield(player1, new OathOfScholars());
        harness.setHand(player1, List.of(new AngelicBlessing(), new AngelicBlessing(), new AngelicBlessing()));
        harness.setHand(player2, List.of(new AngelicBlessing()));
        harness.setLibrary(player2, List.of(new AngelicBlessing(), new AngelicBlessing(), new AngelicBlessing()));

        advanceToUpkeep(player2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player1.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        harness.assertInGraveyard(player2, "Angelic Blessing");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void decliningLeavesTheActivePlayersHandUntouched() {
        harness.addToBattlefield(player1, new OathOfScholars());
        harness.setHand(player1, List.of(new AngelicBlessing(), new AngelicBlessing()));
        harness.setHand(player2, List.of(new AngelicBlessing()));
        harness.setLibrary(player2, List.of(new AngelicBlessing(), new AngelicBlessing(), new AngelicBlessing()));

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Angelic Blessing");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void targetMustStillHaveMoreCardsWhenTheAbilityResolves() {
        harness.addToBattlefield(player1, new OathOfScholars());
        harness.setHand(player1, List.of(new AngelicBlessing(), new AngelicBlessing()));
        harness.setHand(player2, List.of(new AngelicBlessing()));

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.setHand(player2, List.of(new AngelicBlessing(), new AngelicBlessing()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerWhenNoOpponentHasMoreCardsInHand() {
        harness.addToBattlefield(player1, new OathOfScholars());
        harness.setHand(player1, List.of(new AngelicBlessing(), new AngelicBlessing()));
        harness.setHand(player2, List.of(new AngelicBlessing(), new AngelicBlessing()));

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void activePlayerMayDiscardTheirHandOnTheirOwnUpkeep() {
        harness.addToBattlefield(player1, new OathOfScholars());
        harness.setHand(player1, List.of(new AngelicBlessing()));
        harness.setHand(player2, List.of(new AngelicBlessing(), new AngelicBlessing()));
        harness.setLibrary(player1, List.of(new AngelicBlessing(), new AngelicBlessing(), new AngelicBlessing()));

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Angelic Blessing");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }
}
