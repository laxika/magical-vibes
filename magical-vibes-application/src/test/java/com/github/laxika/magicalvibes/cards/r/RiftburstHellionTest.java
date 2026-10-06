package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiftburstHellion.class, Shock.class})
class RiftburstHellionTest extends BaseCardTest {

    @Test
    void disguiseCastsFaceDown() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RiftburstHellion()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Riftburst Hellion").isFaceDown()).isTrue();
    }

    @Test
    void disguiseCostTurnsCreatureFaceUp() {
        Permanent hellion = castFaceDown();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hellion));

        assertThat(hellion.isFaceDown()).isFalse();
    }

    @Test
    void faceDownCreatureHasWardAndHidesReach() {
        Permanent hellion = castFaceDown();

        assertThat(gqs.getEffectivePower(gd, hellion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hellion)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hellion, Keyword.WARD)).isTrue();
        assertThat(gqs.hasKeyword(gd, hellion, Keyword.REACH)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hellion));

        assertThat(hellion.isFaceDown()).isFalse();
        assertThat(gqs.hasKeyword(gd, hellion, Keyword.WARD)).isFalse();
        assertThat(gqs.hasKeyword(gd, hellion, Keyword.REACH)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void disguiseCostAcceptsOneRedAndOneGreen() {
        Permanent hellion = castFaceDown();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hellion));

        assertThat(hellion.isFaceDown()).isFalse();
    }

    @Test
    void genericManaCannotPayTheHybridSymbols() {
        Permanent hellion = castFaceDown();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(hellion)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(hellion.isFaceDown()).isTrue();
    }

    @Test
    void wardCountersOpponentsSpellWhenTheyCannotPay() {
        Permanent hellion = castFaceDown();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, hellion.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Riftburst Hellion");
        assertThat(hellion.getMarkedDamage()).isZero();
    }

    @Test
    void payingTwoManaForWardLetsOpponentsSpellResolve() {
        Permanent hellion = castFaceDown();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, hellion.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Riftburst Hellion");
        harness.assertNotOnBattlefield(player1, "Riftburst Hellion");
    }

    private Permanent castFaceDown() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RiftburstHellion()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Riftburst Hellion");
    }
}
