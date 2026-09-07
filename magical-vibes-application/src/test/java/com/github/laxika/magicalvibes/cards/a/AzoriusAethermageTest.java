package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AzoriusAethermage.class, Boomerang.class, Forest.class, GrizzlyBears.class, Island.class})
class AzoriusAethermageTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} after a permanent returns to your hand draws a card")
    void payingAfterPermanentReturnsDrawsCard() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new AzoriusAethermage());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        castAndResolveBounce(target.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Declining the payment after a permanent returns draws no card")
    void decliningPaymentDrawsNoCard() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new AzoriusAethermage());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        castAndResolveBounce(target.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(target.getOriginalCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Returning Azorius Aethermage itself still triggers its ability")
    void returningItselfTriggersItsAbility() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent aethermage = harness.addToBattlefieldAndReturn(player1, new AzoriusAethermage());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        castAndResolveBounce(aethermage.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Azorius Aethermage"));
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Returning an opponent's permanent does not trigger Azorius Aethermage")
    void returningOpponentsPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new AzoriusAethermage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getOriginalCard());
    }

    private void castAndResolveBounce(UUID targetId) {
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }
}
