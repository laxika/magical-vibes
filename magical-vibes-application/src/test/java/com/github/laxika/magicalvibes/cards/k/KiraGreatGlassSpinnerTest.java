package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KiraGreatGlassSpinnerTest extends BaseCardTest {

    private void addKira() {
        harness.addToBattlefield(player1, new KiraGreatGlassSpinner());
        findPermanent(player1, "Kira, Great Glass-Spinner").setSummoningSick(false);
    }

    private UUID addBears() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.setSummoningSick(false);
        return bears.getId();
    }

    @Test
    @DisplayName("Counters the first spell targeting another creature you control")
    void countersFirstSpellTargetingOtherCreature() {
        addKira();
        UUID bearsId = addBears();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bearsId);

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }

    @Test
    @DisplayName("Kira grants the ability to itself as well")
    void countersSpellTargetingKira() {
        addKira();
        UUID kiraId = findPermanent(player1, "Kira, Great Glass-Spinner").getId();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, kiraId);

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kira, Great Glass-Spinner");
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }

    @Test
    @DisplayName("A second spell targeting the same creature that turn resolves")
    void secondSpellSameTurnResolves() {
        addKira();
        UUID bearsId = addBears();

        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.castInstant(player2, 0, bearsId);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent's creatures do not get the ability")
    void opponentCreaturesUnaffected() {
        addKira();
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID theirBearsId = findPermanent(player2, "Grizzly Bears").getId();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, theirBearsId);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The grant ends when Kira leaves the battlefield")
    void grantEndsWhenKiraLeaves() {
        addKira();
        UUID bearsId = addBears();

        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Kira, Great Glass-Spinner"));

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bearsId);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
