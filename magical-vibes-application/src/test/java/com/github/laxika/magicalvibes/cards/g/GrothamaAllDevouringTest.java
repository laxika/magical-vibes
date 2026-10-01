package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrothamaAllDevouring.class, GrizzlyBears.class})
class GrothamaAllDevouringTest extends BaseCardTest {

    @Test
    @DisplayName("The attacking creature's controller may have it fight Grothama")
    void attackingCreatureMayFightGrothama() {
        Permanent grothama = addCreatureReady(player1, new GrothamaAllDevouring());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.damageDealtToPermanentsThisTurn.get(grothama.getId())).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(attacker.getMarkedDamage()).isEqualTo(10);
    }

    @Test
    @DisplayName("Each player draws damage dealt by sources they controlled")
    void drawsByDamageSourceController() {
        Permanent grothama = addCreatureReady(player1, new GrothamaAllDevouring());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, grothama));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }
}
