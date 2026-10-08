package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.o.ObeliskOfEsper;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WindwrightMage.class, ObeliskOfEsper.class, Cancel.class})
class WindwrightMageTest extends BaseCardTest {

    @Test
    @DisplayName("No flying when graveyard is empty")
    void noFlyingWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new WindwrightMage());

        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No flying with only a non-artifact card in graveyard")
    void noFlyingWithNonArtifactCard() {
        harness.setGraveyard(player1, List.of(new Cancel()));
        harness.addToBattlefield(player1, new WindwrightMage());

        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Has flying with an artifact card in graveyard")
    void hasFlyingWithArtifactCard() {
        harness.setGraveyard(player1, List.of(new ObeliskOfEsper()));
        harness.addToBattlefield(player1, new WindwrightMage());

        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Loses flying when the artifact card leaves the graveyard")
    void losesFlyingWhenArtifactRemoved() {
        harness.setGraveyard(player1, List.of(new ObeliskOfEsper()));
        harness.addToBattlefield(player1, new WindwrightMage());

        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isTrue();

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Opponent's artifact card in graveyard does not grant flying")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(new ObeliskOfEsper()));
        harness.addToBattlefield(player1, new WindwrightMage());

        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gains flying as soon as an artifact card enters the graveyard")
    void gainsFlyingWhenArtifactEntersGraveyard() {
        harness.addToBattlefield(player1, new WindwrightMage());
        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isFalse();

        harness.setGraveyard(player1, List.of(new ObeliskOfEsper()));

        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Keeps flying while at least one artifact card remains in the graveyard")
    void keepsFlyingWhenOneOfSeveralArtifactsLeaves() {
        harness.setGraveyard(player1, List.of(new ObeliskOfEsper(), new WindwrightMage()));
        harness.addToBattlefield(player1, new WindwrightMage());
        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isTrue();

        harness.setGraveyard(player1, List.of(new WindwrightMage()));

        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("An artifact on the battlefield does not grant flying")
    void battlefieldArtifactDoesNotCount() {
        harness.addToBattlefield(player1, new WindwrightMage());
        harness.addToBattlefield(player1, new ObeliskOfEsper());

        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Lifelink gains life from unblocked combat damage without an artifact in the graveyard")
    void lifelinkWorksWithoutFlying() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WindwrightMage());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Lifelink gains life for both controllers when attacking and blocking mages die")
    void lifelinkWorksWhenBothMagesDieInCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WindwrightMage());
        addCreatureReady(player2, new WindwrightMage());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 22);
        harness.assertInGraveyard(player1, "Windwright Mage");
        harness.assertInGraveyard(player2, "Windwright Mage");
        harness.assertNotOnBattlefield(player1, "Windwright Mage");
        harness.assertNotOnBattlefield(player2, "Windwright Mage");
    }

    @Test
    @DisplayName("Flying prevents a ground mage from blocking until the defender also has an artifact in the graveyard")
    void flyingChangesBlockLegalityForEachController() {
        Permanent attacker = addCreatureReady(player1, new WindwrightMage());
        Permanent blocker = addCreatureReady(player2, new WindwrightMage());
        List<Permanent> defenders = gd.playerBattlefields.get(player2.getId());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker, defenders)).isTrue();

        harness.setGraveyard(player1, List.of(new ObeliskOfEsper()));
        assertThat(bls.canBlockAttacker(gd, blocker, attacker, defenders)).isFalse();

        harness.setGraveyard(player2, List.of(new ObeliskOfEsper()));
        assertThat(bls.canBlockAttacker(gd, blocker, attacker, defenders)).isTrue();

        harness.setGraveyard(player2, List.of());
        assertThat(bls.canBlockAttacker(gd, blocker, attacker, defenders)).isFalse();
    }

    private Permanent findMage() {
        return findPermanent(player1, "Windwright Mage");
    }
}
