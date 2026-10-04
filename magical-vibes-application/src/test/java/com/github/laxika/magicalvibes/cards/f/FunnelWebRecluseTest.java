package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FunnelWebRecluse.class, GrizzlyBears.class, Shock.class})
class FunnelWebRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Does not investigate without morbid")
    void doesNotInvestigateWithoutMorbid() {
        harness.setHand(player1, List.of(new FunnelWebRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Investigates when a creature died this turn")
    void investigatesWithMorbid() {
        harness.setHand(player1, List.of(new FunnelWebRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("A creature killed this turn enables morbid")
    void creatureDeathEnablesMorbid() {
        harness.setHand(player1, List.of(new Shock(), new FunnelWebRecluse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("A death after entry does not retroactively trigger investigate")
    void deathAfterEntryDoesNotTriggerInvestigate() {
        harness.setHand(player1, List.of(new FunnelWebRecluse(), new Shock()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An allied creature death enables a Clue that can be sacrificed to draw")
    void alliedDeathCreatesUsableClue() {
        harness.setHand(player1, List.of(new Shock(), new FunnelWebRecluse()));
        harness.setLibrary(player1, List.of(new FunnelWebRecluse()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Clue"));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, clueIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Funnel-Web Recluse");
    }
}
