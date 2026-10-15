package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SkyDiamond;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeadlyTempest.class, GrizzlyBears.class, DarksteelSentinel.class, SkyDiamond.class})
class DeadlyTempestTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and each player loses life for creatures they controlled")
    void destroysCreaturesAndCausesPerControllerLifeLoss() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new SkyDiamond());

        castDeadlyTempest();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player2, "Sky Diamond");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonCreature);
    }

    @Test
    @DisplayName("Indestructible creatures survive and do not cause life loss")
    void indestructibleCreaturesDoNotCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent indestructible = harness.addToBattlefieldAndReturn(player2, new DarksteelSentinel());

        castDeadlyTempest();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(indestructible);
    }

    @Test
    @DisplayName("No creatures means no life loss")
    void noCreaturesMeansNoLifeLoss() {
        harness.addToBattlefield(player2, new SkyDiamond());

        castDeadlyTempest();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Regenerated creatures survive and do not count toward life loss")
    void regeneratedCreaturesDoNotCount() {
        Permanent regenerated = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        regenerated.setRegenerationShield(1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDeadlyTempest();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(regenerated.isTapped()).isTrue();
        assertThat(regenerated.getRegenerationShield()).isZero();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Life loss follows the creature's controller rather than its owner")
    void stolenCreatureCountsForItsController() {
        GrizzlyBears stolen = new GrizzlyBears();
        stolen.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, stolen);

        castDeadlyTempest();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Only indestructible creatures means no life loss for either player")
    void allIndestructibleCreaturesMeanNoLifeLoss() {
        harness.addToBattlefield(player1, new DarksteelSentinel());
        harness.addToBattlefield(player2, new DarksteelSentinel());

        castDeadlyTempest();

        harness.assertOnBattlefield(player1, "Darksteel Sentinel");
        harness.assertOnBattlefield(player2, "Darksteel Sentinel");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void castDeadlyTempest() {
        harness.setHand(player1, List.of(new DeadlyTempest()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
