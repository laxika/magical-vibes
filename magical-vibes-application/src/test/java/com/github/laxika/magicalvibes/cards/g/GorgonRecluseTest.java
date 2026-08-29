package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GorgonRecluse.class, GiantSpider.class, RavensCrime.class})
class GorgonRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature it blocks at end of combat")
    void destroysNonblackCreatureItBlocksAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        Permanent recluse = addCreatureReady(player2, new GorgonRecluse());

        declareAttackers(player1, List.of(0));
        declareBlocker(recluse, attacker);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Destroys a nonblack creature that blocks it at end of combat")
    void destroysNonblackCreatureThatBlocksItAtEndOfCombat() {
        Permanent recluse = addCreatureReady(player1, new GorgonRecluse());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackers(player1, List.of(0));
        declareBlocker(blocker, recluse);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Does not destroy a black creature")
    void doesNotDestroyBlackCreature() {
        Permanent recluse = addCreatureReady(player1, new GorgonRecluse());
        Permanent blocker = addCreatureReady(player2, new GorgonRecluse());

        declareAttackers(player1, List.of(0));
        declareBlocker(blocker, recluse);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(recluse);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Can cast Gorgon Recluse for {B}{B} after being discarded")
    void castsForMadness() {
        GorgonRecluse recluse = new GorgonRecluse();
        harness.setHand(player1, List.of(recluse));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gorgon Recluse");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(recluse.getId()));
    }

    private void declareBlocker(Permanent blocker, Permanent attacker) {
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
