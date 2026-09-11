package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BilboLuckwearer.class, BurglarPlot.class, Forest.class,
        GrizzlyBears.class, HillGiant.class, Millstone.class})
class BilboLuckwearerTest extends BaseCardTest {

    @Test
    void cannotBeBlocked() {
        Permanent attacker = addCreatureReady(player1, new BilboLuckwearer());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void combatDamageDrawsThenDiscards() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        Permanent attacker = addCreatureReady(player1, new BilboLuckwearer());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void adventureExchangesControlOfMatchingNonlandPermanents() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareAdventure();

        harness.castAdventure(player1, 0, List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
    }

    @Test
    void adventureRejectsLandTarget() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareAdventure();

        assertThatThrownBy(() -> harness.castAdventure(
                player1, 0, List.of(ownCreature.getId(), opponentLand.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    void adventureRejectsPermanentsWithNoSharedCardType() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        prepareAdventure();

        assertThatThrownBy(() -> harness.castAdventure(
                player1, 0, List.of(ownCreature.getId(), opponentArtifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("share a card type");
    }

    private void prepareAdventure() {
        harness.setHand(player1, List.of(new BilboLuckwearer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
