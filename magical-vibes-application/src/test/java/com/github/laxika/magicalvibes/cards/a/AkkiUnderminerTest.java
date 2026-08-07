package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AkkiUnderminerTest extends BaseCardTest {

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("The damaged player chooses one of their own permanents to sacrifice")
    void damagedPlayerChoosesPermanent() {
        Permanent underminer = addCreatureReady(player1, new AkkiUnderminer());
        underminer.setAttacking(true);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent enemyLand = addPermanent(player2, new Mountain());

        resolveCombat();
        harness.passBothPriorities(); // resolve sacrifice trigger

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds())
                .contains(enemyCreature.getId(), enemyLand.getId())
                .doesNotContain(ownCreature.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(enemyLand.getId()));

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Blocked creature deals no combat damage to the player, so nothing is sacrificed")
    void noSacrificeWhenBlocked() {
        Permanent underminer = addCreatureReady(player1, new AkkiUnderminer());
        underminer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent enemyLand = addPermanent(player2, new Mountain());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(enemyLand.getId()).isNotNull();
    }

    @Test
    @DisplayName("Nothing happens when the damaged player controls no permanents")
    void noPermanentsToSacrifice() {
        Permanent underminer = addCreatureReady(player1, new AkkiUnderminer());
        underminer.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
