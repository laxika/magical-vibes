package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GladecoverScout;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HuntedNightmare;
import com.github.laxika.magicalvibes.cards.b.BaneslayerAngel;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.cards.z.ZetalpaPrimalDawn;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        BleedingEffect.class,
        GrizzlyBears.class,
        ZetalpaPrimalDawn.class,
        BaneslayerAngel.class,
        VampireNighthawk.class,
        GladecoverScout.class,
        HuntedNightmare.class,
        GiantSpider.class
})
class BleedingEffectTest extends BaseCardTest {

    @Test
    @DisplayName("Shares every listed keyword found on creature cards in the controller's graveyard")
    void sharesKeywordsFromOwnGraveyard() {
        Permanent bleedingEffect = harness.addToBattlefieldAndReturn(player1, new BleedingEffect());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new ZetalpaPrimalDawn(),
                new BaneslayerAngel(),
                new VampireNighthawk(),
                new GladecoverScout(),
                new HuntedNightmare(),
                new GiantSpider()
        ));

        advanceToCombatAndResolve(player1);

        assertThat(gqs.computeStaticBonus(gd, bears).keywords()).contains(
                Keyword.FLYING,
                Keyword.FIRST_STRIKE,
                Keyword.DOUBLE_STRIKE,
                Keyword.DEATHTOUCH,
                Keyword.HEXPROOF,
                Keyword.INDESTRUCTIBLE,
                Keyword.LIFELINK,
                Keyword.MENACE,
                Keyword.REACH,
                Keyword.TRAMPLE,
                Keyword.VIGILANCE
        );
        assertThat(gqs.computeStaticBonus(gd, opponentBears).keywords()).isEmpty();
        assertThat(gqs.computeStaticBonus(gd, bleedingEffect).keywords()).isEmpty();
    }

    @Test
    @DisplayName("Checks the graveyard when the trigger resolves and only uses the controller's graveyard")
    void checksOwnGraveyardAtResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new ZetalpaPrimalDawn()));
        harness.addToBattlefield(player1, new BleedingEffect());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of(new ZetalpaPrimalDawn()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void keywordsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BleedingEffect());
        harness.setGraveyard(player1, List.of(new ZetalpaPrimalDawn()));

        advanceToCombatAndResolve(player1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
