package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CloudcrestLake;
import com.github.laxika.magicalvibes.cards.j.JukaiMessenger;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkkiUnderminer.class, JukaiMessenger.class, CloudcrestLake.class})
class AkkiUnderminerTest extends BaseCardTest {

    @Test
    @DisplayName("The damaged player chooses one of their own permanents to sacrifice")
    void damagedPlayerChoosesPermanent() {
        Permanent underminer = addCreatureReady(player1, new AkkiUnderminer());
        underminer.setAttacking(true);
        Permanent ownCreature = addCreatureReady(player1, new JukaiMessenger());
        Permanent enemyCreature = addCreatureReady(player2, new JukaiMessenger());
        Permanent enemyLand = harness.addToBattlefieldAndReturn(player2, new CloudcrestLake());

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

        harness.assertNotOnBattlefield(player2, "Cloudcrest Lake");
        harness.assertOnBattlefield(player2, "Jukai Messenger");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Blocked creature deals no combat damage to the player, so nothing is sacrificed")
    void noSacrificeWhenBlocked() {
        Permanent underminer = addCreatureReady(player1, new AkkiUnderminer());
        underminer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new JukaiMessenger());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.addToBattlefield(player2, new CloudcrestLake());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Cloudcrest Lake");
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

    @Test
    @DisplayName("The damaged player's only permanent is sacrificed without a choice")
    void sacrificesOnlyPermanentWithoutChoice() {
        Permanent underminer = addCreatureReady(player1, new AkkiUnderminer());
        underminer.setAttacking(true);
        harness.addToBattlefield(player2, new CloudcrestLake());

        resolveCombat();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cloudcrest Lake");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
