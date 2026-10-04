package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LightningAxe;
import com.github.laxika.magicalvibes.cards.p.PenumbraSpider;
import com.github.laxika.magicalvibes.cards.t.ThallidShellDweller;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GorgonRecluse.class, LightningAxe.class, PenumbraSpider.class, ThallidShellDweller.class})
class GorgonRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature it blocks at end of combat")
    void destroysNonblackCreatureItBlocksAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new PenumbraSpider());
        Permanent recluse = addCreatureReady(player2, new GorgonRecluse());

        declareAttackers(player1, List.of(0));
        declareBlockers(attacker, recluse);

        resolveCombat();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Penumbra Spider");
    }

    @Test
    @DisplayName("Destroys a nonblack creature that blocks it at end of combat")
    void destroysNonblackCreatureThatBlocksItAtEndOfCombat() {
        Permanent recluse = addCreatureReady(player1, new GorgonRecluse());
        Permanent blocker = addCreatureReady(player2, new PenumbraSpider());

        declareAttackers(player1, List.of(0));
        declareBlockers(recluse, blocker);

        resolveCombat();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Penumbra Spider");
    }

    @Test
    @DisplayName("Destroys every nonblack creature that blocks it")
    void destroysEveryNonblackCreatureThatBlocksIt() {
        Permanent recluse = addCreatureReady(player1, new GorgonRecluse());
        Permanent firstBlocker = addCreatureReady(player2, new ThallidShellDweller());
        Permanent secondBlocker = addCreatureReady(player2, new ThallidShellDweller());

        declareAttackers(player1, List.of(0));
        declareBlockers(recluse, firstBlocker, secondBlocker);

        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(firstBlocker, secondBlocker);

        resolveCombat();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                secondBlocker.getId(), 1
        ));

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(firstBlocker.getCard(), secondBlocker.getCard());
    }

    @Test
    @DisplayName("Does not destroy a black creature")
    void doesNotDestroyBlackCreature() {
        Permanent recluse = addCreatureReady(player1, new GorgonRecluse());
        Permanent blocker = addCreatureReady(player2, new GorgonRecluse());

        declareAttackers(player1, List.of(0));
        declareBlockers(recluse, blocker);
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(recluse);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Can cast Gorgon Recluse for {B}{B} after being discarded")
    void castsForMadness() {
        GorgonRecluse recluse = new GorgonRecluse();
        Permanent target = addCreatureReady(player1, new ThallidShellDweller());
        harness.setHand(player2, List.of(new LightningAxe(), recluse));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstantWithDiscard(player2, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Gorgon Recluse");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(recluse.getId()));
    }

    @Test
    @DisplayName("Puts Gorgon Recluse into its owner's graveyard when madness is declined")
    void declinesMadness() {
        GorgonRecluse recluse = new GorgonRecluse();
        Permanent target = addCreatureReady(player1, new ThallidShellDweller());
        harness.setHand(player2, List.of(new LightningAxe(), recluse));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstantWithDiscard(player2, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(recluse.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(recluse.getId()));
    }

    @Test
    @DisplayName("A black attacker survives being blocked by Gorgon Recluse")
    void doesNotDestroyBlackCreatureItBlocks() {
        Permanent attacker = addCreatureReady(player1, new GorgonRecluse());
        Permanent blocker = addCreatureReady(player2, new GorgonRecluse());

        declareAttackers(player1, List.of(0));
        declareBlockers(attacker, blocker);
        resolveCombat();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Delayed destruction still happens after Gorgon Recluse dies in combat")
    void destroysBlockersAfterDyingInCombat() {
        Permanent recluse = addCreatureReady(player1, new GorgonRecluse());
        Permanent firstBlocker = addCreatureReady(player2, new PenumbraSpider());
        Permanent secondBlocker = addCreatureReady(player2, new PenumbraSpider());

        declareAttackers(player1, List.of(0));
        declareBlockers(recluse, firstBlocker, secondBlocker);
        resolveAllTriggers();
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                secondBlocker.getId(), 1
        ));

        harness.assertInGraveyard(player1, "Gorgon Recluse");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firstBlocker, secondBlocker);

        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(firstBlocker.getCard(), secondBlocker.getCard());
    }

    private void declareBlockers(Permanent attacker, Permanent... blockers) {
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(blockers).stream()
                .map(blocker -> new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))
                .toList());
    }
}
