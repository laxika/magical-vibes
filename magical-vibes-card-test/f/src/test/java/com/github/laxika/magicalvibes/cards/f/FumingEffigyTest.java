package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FumingEffigy.class, Disentomb.class, GrizzlyBears.class, Reminisce.class})
class FumingEffigyTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to each opponent when a card leaves the controller's graveyard")
    void dealsDamageWhenCardLeavesGraveyard() {
        addFumingEffigy(player1);
        harness.setLife(player2, 20);

        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Triggers only once when multiple cards leave the graveyard in one event")
    void triggersOnceForBatchedGraveyardDeparture() {
        addFumingEffigy(player1);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void doesNotTriggerForOpponentsGraveyard() {
        addFumingEffigy(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotTriggerWhenNoCardsLeave() {
        addFumingEffigy(player1);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void triggersForNoncreatureCardsAndDoesNotDamageController() {
        addFumingEffigy(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new Disentomb(), new Reminisce()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void eachEffigyTriggersOnceForTheSameBatch() {
        addFumingEffigy(player1);
        addFumingEffigy(player1);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    private void addFumingEffigy(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new FumingEffigy());
    }
}
