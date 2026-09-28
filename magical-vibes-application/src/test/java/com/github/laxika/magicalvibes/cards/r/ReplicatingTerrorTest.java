package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReplicatingTerror.class, GrizzlyBears.class})
@DisplayName("Replicating Terror")
class ReplicatingTerrorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices an opponent's nontoken creature and conjures its duplicate into your graveyard")
    void sacrificesAndConjuresDuplicate() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castReplicatingTerror();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).anySatisfy(card -> {
            assertThat(card.getName()).isEqualTo("Grizzly Bears");
            assertThat(card.getId()).isNotEqualTo(creature.getCard().getId());
        });
    }

    @Test
    @DisplayName("The opponent chooses which nontoken creature to sacrifice")
    void opponentChoosesCreature() {
        Permanent kept = addCreatureReady(player2, new GrizzlyBears());
        Permanent sacrificed = addCreatureReady(player2, new GrizzlyBears());

        castReplicatingTerror();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player2, List.of(sacrificed.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kept);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(sacrificed);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card ->
                card.getName().equals("Grizzly Bears")
                        && !card.getId().equals(sacrificed.getCard().getId()));
    }

    @Test
    @DisplayName("Does not sacrifice or copy creature tokens")
    void ignoresCreatureTokens() {
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = addCreatureReady(player2, tokenCard);

        castReplicatingTerror();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card ->
                card.getName().equals("Grizzly Bears"));
    }

    private void castReplicatingTerror() {
        harness.setHand(player1, List.of(new ReplicatingTerror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
