package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AesthirGlider;
import com.github.laxika.magicalvibes.cards.w.WildAesthir;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StenchOfDecay.class, StormCrow.class, AesthirGlider.class, WildAesthir.class})
class StenchOfDecayTest extends BaseCardTest {

    @Test
    @DisplayName("Nonartifact creatures on both sides get -1/-1, artifact creatures unaffected")
    void shrinksNonartifactCreaturesOnly() {
        Permanent ownCrow = addCreatureReady(player1, new StormCrow());
        Permanent opponentCrow = addCreatureReady(player2, new StormCrow());
        Permanent glider = addCreatureReady(player2, new AesthirGlider());

        castStench();

        assertThat(gqs.getEffectivePower(gd, ownCrow)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, ownCrow)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentCrow)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opponentCrow)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, glider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, glider)).isEqualTo(1);
    }

    @Test
    @DisplayName("1/1 creatures die to the -1/-1")
    void onlyOneToughnessCreaturesDie() {
        Permanent bird = addCreatureReady(player2, new WildAesthir());

        castStench();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bird);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bird.getCard());
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent crow = addCreatureReady(player1, new StormCrow());

        castStench();

        assertThat(gqs.getEffectivePower(gd, crow)).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, crow)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crow)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution are not affected")
    void doesNotAffectCreaturesEnteringAfterResolution() {
        castStench();

        Permanent laterCrow = addCreatureReady(player1, new StormCrow());

        assertThat(gqs.getEffectivePower(gd, laterCrow)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, laterCrow)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering while the spell is on the stack are affected")
    void affectsCreaturesEnteringBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new StenchOfDecay(), "{1}{B}{B}");

        Permanent crow = addCreatureReady(player2, new StormCrow());
        Permanent glider = addCreatureReady(player2, new AesthirGlider());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crow)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, crow)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, glider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, glider)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple casts accumulate and kill two-toughness nonartifact creatures")
    void multipleCastsAccumulate() {
        Permanent ownCrow = addCreatureReady(player1, new StormCrow());
        Permanent opponentCrow = addCreatureReady(player2, new StormCrow());
        Permanent glider = addCreatureReady(player2, new AesthirGlider());

        castStench();
        castStench();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCrow);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCrow.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCrow);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCrow.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(glider);
        assertThat(gqs.getEffectivePower(gd, glider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, glider)).isEqualTo(1);
    }

    private void castStench() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new StenchOfDecay(), "{1}{B}{B}");
        harness.passBothPriorities();
    }
}
