package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({ShrineOfLoyalLegions.class, SuntailHawk.class, GrizzlyBears.class})
class ShrineOfLoyalLegionsTest extends BaseCardTest {

    private Permanent getShrine() {
        return findPermanent(player1, "Shrine of Loyal Legions");
    }

    private int getShrineIndex() {
        var battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent shrine = getShrine();
        return battlefield.indexOf(shrine);
    }

    @Test
    @DisplayName("Upkeep trigger puts a charge counter on Shrine")
    void upkeepAddsChargeCounter() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getShrine().getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple upkeeps accumulate charge counters")
    void multipleUpkeepsAccumulateCounters() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve first upkeep trigger

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve second upkeep trigger

        assertThat(getShrine().getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void noTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(getShrine().getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a white spell puts a charge counter on Shrine")
    void whiteSpellAddsChargeCounter() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(getShrine().getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-white spell does not add a charge counter")
    void nonWhiteSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(getShrine().getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent casting a white spell does not add a charge counter")
    void opponentWhiteSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());
        harness.setHand(player2, List.of(new SuntailHawk()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(getShrine().getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Sacrificing with charge counters creates 1/1 Myr tokens")
    void sacrificeCreatesTokens() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());
        getShrine().setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, getShrineIndex(), null, null);
        harness.passBothPriorities(); // resolve activated ability

        // Shrine is sacrificed (no longer on battlefield)
        harness.assertNotOnBattlefield(player1, "Shrine of Loyal Legions");

        // 3 Myr tokens created
        long myrCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr")
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1
                        && p.getCard().hasType(CardType.ARTIFACT))
                .count();
        assertThat(myrCount).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificing with 5 charge counters creates 5 tokens")
    void sacrificeWithFiveCountersCreatesFiveTokens() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());
        getShrine().setCounterCount(CounterType.CHARGE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, getShrineIndex(), null, null);
        harness.passBothPriorities();

        long myrCount = countPermanents(player1, "Myr");
        assertThat(myrCount).isEqualTo(5);
    }

    @Test
    @DisplayName("Sacrificing with 0 charge counters creates no tokens")
    void sacrificeWithZeroCountersCreatesNoTokens() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());
        getShrine().setCounterCount(CounterType.CHARGE, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, getShrineIndex(), null, null);
        harness.passBothPriorities();

        // Shrine is sacrificed
        harness.assertNotOnBattlefield(player1, "Shrine of Loyal Legions");

        // No tokens
        harness.assertNotOnBattlefield(player1, "Myr");
    }

    @Test
    @DisplayName("Accumulate counters via upkeep and white spells, then sacrifice for tokens")
    void fullLifecycle() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());

        // Upkeep: +1 counter
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger
        assertThat(getShrine().getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        // Cast white spell: +1 counter
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(getShrine().getCounterCount(CounterType.CHARGE)).isEqualTo(2);

        // Sacrifice shrine with 2 counters
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, getShrineIndex(), null, null);
        harness.passBothPriorities();

        long myrCount = countPermanents(player1, "Myr");
        assertThat(myrCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, before token creation resolves")
    void sacrificeIsAnActivationCost() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());
        getShrine().setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, getShrineIndex(), null, null);

        harness.assertNotOnBattlefield(player1, "Shrine of Loyal Legions");
        harness.assertInGraveyard(player1, "Shrine of Loyal Legions");
        harness.assertNotOnBattlefield(player1, "Myr");

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Myr")).isEqualTo(2);
        assertThat(countPermanents(player2, "Myr")).isZero();
        assertThat(findPermanents(player1, "Myr")).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColors()).isEmpty();
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.MYR);
            assertThat(token.isTapped()).isFalse();
        });
    }

    @Test
    @DisplayName("A pending upkeep trigger cannot increase tokens after sacrifice")
    void sacrificeInResponseToUpkeepUsesExistingCounters() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());
        getShrine().setCounterCount(CounterType.CHARGE, 2);
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, getShrineIndex(), null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Myr")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shrine of Loyal Legions");
    }

    @Test
    @DisplayName("A tapped Shrine cannot pay the activation's tap cost")
    void tappedShrineCannotActivate() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());
        getShrine().tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, getShrineIndex(), null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Shrine of Loyal Legions");
        harness.assertNotOnBattlefield(player1, "Myr");
    }

    @Test
    @DisplayName("Activation requires three mana")
    void insufficientManaCannotActivate() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, getShrineIndex(), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(getShrine().isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Myr");
    }

    @Test
    @DisplayName("A white creature entering without being cast does not trigger Shrine")
    void whiteCreatureEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());
        harness.addToBattlefield(player1, new SuntailHawk());

        resolveAllTriggers();

        assertThat(getShrine().getCounterCount(CounterType.CHARGE)).isZero();
    }
}
