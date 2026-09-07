package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.ExperimentalAviator;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VirulentSilencer.class, ExperimentalAviator.class, GrizzlyBears.class})
class VirulentSilencerTest extends BaseCardTest {

    private Permanent addReady(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    @DisplayName("A nontoken artifact creature dealing combat damage gives two poison counters")
    void nontokenArtifactCreatureGivesTwoPoisonCounters() {
        addReady(new VirulentSilencer()).setAttacking(true);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-artifact creature does not trigger Virulent Silencer")
    void nonArtifactCreatureDoesNotTrigger() {
        addReady(new VirulentSilencer());
        addReady(new GrizzlyBears()).setAttacking(true);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("A token artifact creature does not trigger Virulent Silencer")
    void tokenArtifactCreatureDoesNotTrigger() {
        addReady(new VirulentSilencer());
        harness.enterBattlefieldAndReturn(player1, new ExperimentalAviator());
        resolveAllTriggers();

        Permanent thopter = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        thopter.setSummoningSick(false);
        thopter.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
