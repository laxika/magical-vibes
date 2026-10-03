package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({BrimstoneRoundup.class, Shock.class, GrizzlyBears.class})
class BrimstoneRoundupTest extends BaseCardTest {

    @Test
    void roundupCastAsFirstSpellCountsTowardSecondSpell() {
        harness.setHand(player1, List.of(new BrimstoneRoundup(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Mercenary")).isZero();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Mercenary")).isEqualTo(1);
    }

    @Test
    void roundupDoesNotTriggerForItsOwnCastAsSecondSpell() {
        harness.setHand(player1, List.of(new Shock(), new BrimstoneRoundup(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(countPermanents(player1, "Mercenary")).isZero();
    }

    @Test
    void opponentsSpellsDoNotTriggerRoundup() {
        harness.addToBattlefield(player1, new BrimstoneRoundup());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(countPermanents(player1, "Mercenary")).isZero();
        assertThat(countPermanents(player2, "Mercenary")).isZero();
    }

    @Test
    void secondSpellOnOpponentsTurnCreatesTokenBeforeSpellResolves() {
        harness.addToBattlefield(player1, new BrimstoneRoundup());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Mercenary")).isEqualTo(1);
        harness.assertLife(player2, 18);
        resolveAllTriggers();
        harness.assertLife(player2, 16);
    }

    @Test
    void plotExilesWithoutCastingAndAllowsFreeCastOnLaterTurn() {
        BrimstoneRoundup roundup = new BrimstoneRoundup();
        harness.setHand(player1, List.of(roundup));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());

        harness.assertNotInHand(player1, "Brimstone Roundup");
        harness.assertNotOnBattlefield(player1, "Brimstone Roundup");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isZero();
        assertThatThrownBy(() -> harness.castFromExile(player1, roundup.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("turn it became plotted");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, roundup.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Brimstone Roundup");
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(1);
    }

    @Test
    void plotRequiresFullPlotCostAndSorceryTiming() {
        harness.setHand(player1, List.of(new BrimstoneRoundup()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertInHand(player1, "Brimstone Roundup");
    }

    @Test
    void mercenaryRequiresReadinessAndSorceryTimingButCanBoostItself() {
        harness.addToBattlefield(player1, new BrimstoneRoundup());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        Permanent mercenary = findPermanent(player1, "Mercenary");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        mercenary.setSummoningSick(false);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player1, 0, player2.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        resolveAllTriggers();

        harness.activateAbility(player1, index, 0, null, mercenary.getId());
        resolveAllTriggers();
        assertThat(mercenary.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mercenary)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a second spell creates a Mercenary token")
    void secondSpellCreatesMercenaryToken() {
        harness.addToBattlefield(player1, new BrimstoneRoundup());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Mercenary")).isZero();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Mercenary")).isEqualTo(1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Mercenary")).isEqualTo(1);
    }

    @Test
    @DisplayName("The created Mercenary boosts a creature you control")
    void createdMercenaryBoostsCreatureYouControl() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BrimstoneRoundup());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Mercenary cannot target an opposing creature")
    void mercenaryCannotTargetOpposingCreature() {
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new BrimstoneRoundup());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, mercenaryIndex, 0, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }
}
