package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HavocJesterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing your permanent triggers 1 damage to any target")
    void sacrificeDealsDamageToTargetPlayer() {
        addCreatureReady(player1, new HavocJester());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        sacrifice(bears);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Sacrifice trigger can target a creature")
    void sacrificeDealsDamageToTargetCreature() {
        addCreatureReady(player1, new HavocJester());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        UUID targetId = target.getId();

        sacrifice(bears);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(targetId, player2.getId());
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetId));
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Opponent sacrificing a permanent does not trigger Havoc Jester")
    void opponentSacrificeDoesNotTrigger() {
        addCreatureReady(player1, new HavocJester());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        sacrificeForPlayer(bears, player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void sacrifice(Permanent permanent) {
        sacrificeForPlayer(permanent, player1);
    }

    private void sacrificeForPlayer(Permanent permanent, com.github.laxika.magicalvibes.model.Player player) {
        Card card = permanent.getCard();
        gd.playerBattlefields.get(player.getId()).remove(permanent);
        gd.playerGraveyards.get(player.getId()).add(card);
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player.getId(), card));
    }
}
