package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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
        GrizzlyBears.class, HillGiant.class, Millstone.class, Unsummon.class})
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

    @Test
    void combatDamageCanDiscardTheCardJustDrawn() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        Permanent attacker = addCreatureReady(player1, new BilboLuckwearer());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    void combatDamageWithEmptyHandStillDrawsThenDiscards() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        Permanent attacker = addCreatureReady(player1, new BilboLuckwearer());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertLife(player2, 19);
    }

    @Test
    void adventureExilesBilboAndAllowsCastingCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        BilboLuckwearer bilbo = new BilboLuckwearer();
        harness.setHand(player1, List.of(bilbo));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAdventure(player1, 0, List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(bilbo.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Bilbo, Luckwearer");
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, bilbo.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bilbo, Luckwearer");
        assertThat(gd.findExiledCard(bilbo.getId())).isNull();
    }

    @Test
    void adventureCanTargetTwoPermanentsWithTheSameController() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareAdventure();
        BilboLuckwearer bilbo = (BilboLuckwearer) gd.playerHands.get(player1.getId()).getFirst();

        harness.castAdventure(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.findExiledCard(bilbo.getId())).isNotNull();
    }

    @Test
    void adventureDoesNotExchangeWhenOneTargetLeavesButStillExilesBilbo() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareAdventure();
        BilboLuckwearer bilbo = (BilboLuckwearer) gd.playerHands.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAdventure(player1, 0, List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.castInstant(player2, 0, opponentCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(ownCreature);
        harness.assertInHand(player2, "Hill Giant");
        assertThat(gd.findExiledCard(bilbo.getId())).isNotNull();
    }

    @Test
    void adventureExchangesArtifactsAsWellAsCreatures() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        prepareAdventure();

        harness.castAdventure(player1, 0, List.of(ownArtifact.getId(), opponentArtifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownArtifact).doesNotContain(opponentArtifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentArtifact).doesNotContain(ownArtifact);
    }

    @Test
    void adventureWithAllTargetsGoneGoesToGraveyardInsteadOfExile() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareAdventure();
        BilboLuckwearer bilbo = (BilboLuckwearer) gd.playerHands.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new Unsummon(), new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAdventure(player1, 0, List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.castInstant(player2, 0, ownCreature.getId());
        harness.castInstant(player2, 0, opponentCreature.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Bilbo, Luckwearer");
        assertThat(gd.findExiledCard(bilbo.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(bilbo.getId());
    }

    private void prepareAdventure() {
        harness.setHand(player1, List.of(new BilboLuckwearer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
