package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Crawlspace;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.p.PlagueBeetle;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LastDitchEffort.class, Crawlspace.class, GiantCockroach.class,
        PlagueBeetle.class, YavimayaWurm.class})
class LastDitchEffortTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices any chosen creatures and deals that much damage to a player")
    void sacrificesChosenCreaturesAndDealsTheirCountAsDamage() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new YavimayaWurm());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new PlagueBeetle());
        harness.setLife(player2, 20);
        castLastDitchEffort(player2.getId());

        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                first.getId(), second.getId(), third.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Giant Cockroach", "Yavimaya Wurm");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(third.getId());
    }

    @Test
    @DisplayName("Sacrificing no creatures deals no damage")
    void sacrificingNoCreaturesDealsNoDamage() {
        harness.addToBattlefield(player1, new GiantCockroach());
        harness.setLife(player2, 20);
        castLastDitchEffort(player2.getId());

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Giant Cockroach");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Having no creatures to sacrifice requires no choice")
    void havingNoCreaturesToSacrificeRequiresNoChoice() {
        harness.addToBattlefield(player1, new Crawlspace());
        harness.setLife(player2, 20);
        castLastDitchEffort(player2.getId());

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Crawlspace");
    }

    @Test
    @DisplayName("Only creatures you control are offered for sacrifice")
    void onlyCreaturesYouControlAreOfferedForSacrifice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Crawlspace());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new YavimayaWurm());
        harness.setLife(player2, 20);
        castLastDitchEffort(player2.getId());

        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(creature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Giant Cockroach");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(artifact.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(opponentCreature.getId());
    }

    @Test
    @DisplayName("Deals damage to a creature equal to the number of creatures sacrificed")
    void dealsDamageToCreature() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new PlagueBeetle());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YavimayaWurm());
        castLastDitchEffort(target.getId());

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(sacrificed.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    private void castLastDitchEffort(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new LastDitchEffort()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
    }
}
