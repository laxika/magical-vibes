package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.Abundance;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToxicIguanar.class, GrizzlyBears.class, HillGiant.class, Abundance.class})
class ToxicIguanarTest extends BaseCardTest {


    @Test
    @DisplayName("Has deathtouch while controlling a green permanent")
    void hasDeathtouchWithGreenPermanent() {
        Permanent iguanar = harness.addToBattlefieldAndReturn(player1, new ToxicIguanar());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, iguanar, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Does NOT have deathtouch without a green permanent")
    void noDeathtouchWithoutGreenPermanent() {
        Permanent iguanar = harness.addToBattlefieldAndReturn(player1, new ToxicIguanar());

        assertThat(gqs.hasKeyword(gd, iguanar, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A non-green permanent does not grant deathtouch")
    void nonGreenPermanentDoesNotGrant() {
        Permanent iguanar = harness.addToBattlefieldAndReturn(player1, new ToxicIguanar());
        harness.addToBattlefield(player1, new HillGiant());

        assertThat(gqs.hasKeyword(gd, iguanar, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A green permanent controlled by an opponent does not grant deathtouch")
    void opponentGreenPermanentDoesNotGrant() {
        Permanent iguanar = harness.addToBattlefieldAndReturn(player1, new ToxicIguanar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, iguanar, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Gains deathtouch dynamically when a green permanent enters")
    void gainsDeathtouchDynamically() {
        Permanent iguanar = harness.addToBattlefieldAndReturn(player1, new ToxicIguanar());

        assertThat(gqs.hasKeyword(gd, iguanar, Keyword.DEATHTOUCH)).isFalse();

        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, iguanar, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("A green noncreature permanent grants deathtouch only to Toxic Iguanar")
    void greenEnchantmentGrantsDeathtouchOnlyToIguanar() {
        Permanent iguanar = harness.addToBattlefieldAndReturn(player1, new ToxicIguanar());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player1, new Abundance());

        assertThat(gqs.hasKeyword(gd, iguanar, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Loses deathtouch immediately when the last green permanent leaves")
    void losesDeathtouchWhenLastGreenPermanentLeaves() {
        Permanent iguanar = harness.addToBattlefieldAndReturn(player1, new ToxicIguanar());
        Permanent green = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, iguanar, Keyword.DEATHTOUCH)).isTrue();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, green));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, iguanar, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Retains deathtouch while another green permanent remains")
    void retainsDeathtouchWhenOneOfTwoGreenPermanentsLeaves() {
        Permanent iguanar = harness.addToBattlefieldAndReturn(player1, new ToxicIguanar());
        Permanent green = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, green));

        assertThat(gqs.hasKeyword(gd, iguanar, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch destroys a larger blocker while a green permanent is controlled")
    void deathtouchDestroysLargerBlocker() {
        addCreatureReady(player1, new ToxicIguanar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Toxic Iguanar");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("A larger blocker survives when no green permanent is controlled")
    void largerBlockerSurvivesWithoutGreenPermanent() {
        addCreatureReady(player1, new ToxicIguanar());
        addCreatureReady(player2, new HillGiant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Toxic Iguanar");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Hill Giant");
    }
}
