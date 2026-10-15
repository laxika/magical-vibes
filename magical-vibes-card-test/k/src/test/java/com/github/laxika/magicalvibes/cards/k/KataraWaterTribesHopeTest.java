package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KataraWaterTribesHope.class, GrizzlyBears.class})
class KataraWaterTribesHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a 1/1 white Ally token")
    void createsAllyToken() {
        harness.castFromHand(player1, new KataraWaterTribesHope(), "{2}{W}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.ALLY);
    }

    @Test
    @DisplayName("Waterbend X sets your creatures' base power and toughness")
    void waterbendSetsOwnCreaturesToX() {
        harness.castFromHand(player1, new KataraWaterTribesHope(), "{2}{W}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent katara = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Katara, Water Tribe's Hope"));
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent allyToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.activateAbility(player1, 0, 3, null);

        assertThat(katara.isTapped()).isTrue();
        assertThat(ownBears.isTapped()).isTrue();
        assertThat(allyToken.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, katara)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, katara)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("X cannot be zero")
    void waterbendRequiresPositiveX() {
        harness.addToBattlefieldAndReturn(player1, new KataraWaterTribesHope());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X must be at least 1");
    }

    @Test
    @DisplayName("Waterbend can be activated only during your turn")
    void waterbendRequiresYourTurn() {
        harness.addToBattlefieldAndReturn(player1, new KataraWaterTribesHope());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("A tapped Katara can waterbend using mana during your end step")
    void tappedKataraCanWaterbendDuringEndStep() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraWaterTribesHope());
        katara.tap();
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 4, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, katara)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, katara)).isEqualTo(4);
    }

    @Test
    @DisplayName("Waterbend's base power and toughness expire at cleanup")
    void waterbendExpiresAtCleanup() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraWaterTribesHope());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 5, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, katara)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, katara)).isEqualTo(5);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, katara)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, katara)).isEqualTo(3);
    }

    @Test
    @DisplayName("Waterbend affects creatures present at resolution, but not later arrivals")
    void affectedCreaturesAreDeterminedAtResolution() {
        harness.addToBattlefieldAndReturn(player1, new KataraWaterTribesHope());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 4, null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
    }

    @Test
    @DisplayName("The latest resolved waterbend sets base stats and preserves counters")
    void latestWaterbendWinsWithoutRemovingCounters() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraWaterTribesHope());
        katara.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 5, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, katara)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, katara)).isEqualTo(7);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, katara)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, katara)).isEqualTo(3);
        assertThat(katara.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Waterbend can combine mana with tapping a summoning-sick creature")
    void waterbendCombinesManaAndSummoningSickCreature() {
        Permanent katara = harness.enterBattlefieldAndReturn(player1, new KataraWaterTribesHope());
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 4, null);
        harness.passBothPriorities();

        assertThat(katara.isTapped() || token.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, katara)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, katara)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }
}
