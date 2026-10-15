package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.Cankerbloom;
import com.github.laxika.magicalvibes.cards.m.MondrakGloryDominus;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrabrasksForge.class, Cankerbloom.class, MondrakGloryDominus.class})
class UrabrasksForgeTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Puts an oil counter on itself and creates a hasty trampling Horror")
    void createsHastyTramplingHorror() {
        harness.addToBattlefield(player1, new UrabrasksForge());

        advanceToCombat(player1);
        harness.passBothPriorities();

        Permanent forge = findPermanent(player1, "Urabrask's Forge");
        Permanent horror = findPermanent(player1, "Phyrexian Horror");

        assertThat(forge.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(1);
        assertThat(horror.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.HORROR);
        assertThat(gqs.hasKeyword(gd, horror, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, horror, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Token power increases with oil counters and the token is sacrificed at the next end step")
    void tokenScalesAndIsSacrificedAtEndStep() {
        harness.addToBattlefield(player1, new UrabrasksForge());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Phyrexian Horror")).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Phyrexian Horror")).isZero();

        advanceToCombat(player1);
        harness.passBothPriorities();

        Permanent forge = findPermanent(player1, "Urabrask's Forge");
        Permanent horror = findPermanent(player1, "Phyrexian Horror");
        assertThat(forge.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentTurn() {
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new UrabrasksForge());

        advanceToCombat(player2);
        resolveAllTriggers();

        assertThat(forge.getCounterCount(CounterType.OIL)).isZero();
        assertThat(countPermanents(player1, "Phyrexian Horror")).isZero();
        assertThat(countPermanents(player2, "Phyrexian Horror")).isZero();
    }

    @Test
    @DisplayName("Each additional combat creates a new token without changing previous tokens")
    void tokenPowerIsFixedAtCreation() {
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new UrabrasksForge());
        forge.setCounterCount(CounterType.OIL, 3);

        advanceToCombat(player1);
        resolveAllTriggers();
        Permanent first = findPermanent(player1, "Phyrexian Horror");
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);

        advanceToCombat(player1);
        resolveAllTriggers();

        assertThat(forge.getCounterCount(CounterType.OIL)).isEqualTo(5);
        assertThat(findPermanents(player1, "Phyrexian Horror")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        Permanent second = findPermanents(player1, "Phyrexian Horror").get(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Phyrexian Horror")).isZero();
    }

    @Test
    @DisplayName("Destroying the Forge in response uses its last oil count and still schedules sacrifice")
    void sourceRemovalDoesNotStopTokenCreationOrSacrifice() {
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new UrabrasksForge());
        forge.setCounterCount(CounterType.OIL, 3);
        harness.addToBattlefield(player2, new Cankerbloom());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        advanceToCombat(player1);
        harness.activateAbility(player2, 0, null, forge.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Urabrask's Forge");
        Permanent horror = findPermanent(player1, "Phyrexian Horror");
        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Phyrexian Horror")).isZero();
    }

    @Test
    @DisplayName("Removing a Forge with no oil counters creates a zero-power token")
    void removedCounterlessForgeCreatesZeroPowerToken() {
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new UrabrasksForge());
        harness.addToBattlefield(player2, new Cankerbloom());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        advanceToCombat(player1);
        harness.activateAbility(player2, 0, null, forge.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Urabrask's Forge");
        Permanent horror = findPermanent(player1, "Phyrexian Horror");
        assertThat(gqs.getEffectivePower(gd, horror)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(1);
    }

    @Test
    @DisplayName("Token doubling creates two tokens and both are sacrificed")
    void sacrificesAllTokensCreatedByReplacement() {
        harness.addToBattlefield(player1, new UrabrasksForge());
        harness.addToBattlefield(player1, new MondrakGloryDominus());

        advanceToCombat(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Phyrexian Horror")).hasSize(2)
                .allSatisfy(token -> {
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                });
        assertThat(findPermanent(player1, "Urabrask's Forge").getCounterCount(CounterType.OIL))
                .isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Phyrexian Horror")).isZero();
        harness.assertOnBattlefield(player1, "Mondrak, Glory Dominus");
    }
}
