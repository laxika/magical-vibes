package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

    @Test
    @DisplayName("Sacrifices the nontoken creature while leaving tokens and your creatures alone")
    void mixedBattlefieldSacrificesOnlyOpponentsNontokenCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = addCreatureReady(player2, tokenCard);

        castReplicatingTerror();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(token);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))).hasSize(1);
    }

    @Test
    @DisplayName("Resolves without conjuring anything when the opponent has no creatures")
    void noOpponentCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        castReplicatingTerror();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card ->
                card.getName().equals("Grizzly Bears"));
        harness.assertInGraveyard(player1, "Replicating Terror");
    }

    @Test
    @DisplayName("The conjured duplicate belongs to the caster when it later leaves the battlefield")
    void duplicateReturnsToCastersGraveyard() {
        Card opponentCard = new GrizzlyBears();
        opponentCard.setOwnerId(player2.getId());
        addCreatureReady(player2, opponentCard);

        castReplicatingTerror();

        Card duplicate = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        harness.setGraveyard(player1, List.of());
        Permanent returnedCreature = harness.enterBattlefieldAndReturn(player1, duplicate);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, returnedCreature));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(duplicate);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
    }

    private void castReplicatingTerror() {
        harness.castFromHand(player1, new ReplicatingTerror(), "{1}{B}");
        harness.passBothPriorities();
    }
}
