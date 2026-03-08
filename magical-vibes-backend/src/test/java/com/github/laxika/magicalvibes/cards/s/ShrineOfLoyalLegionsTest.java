package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShrineOfLoyalLegionsTest extends BaseCardTest {

    private Permanent getShrine() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Shrine of Loyal Legions"))
                .findFirst().orElseThrow();
    }

    private int getShrineIndex() {
        var battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent shrine = getShrine();
        return battlefield.indexOf(shrine);
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // ===== Upkeep trigger =====

    @Test
    @DisplayName("Upkeep trigger puts a charge counter on Shrine")
    void upkeepAddsChargeCounter() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getShrine().getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple upkeeps accumulate charge counters")
    void multipleUpkeepsAccumulateCounters() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve first upkeep trigger

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve second upkeep trigger

        assertThat(getShrine().getChargeCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void noTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(getShrine().getChargeCounters()).isEqualTo(0);
    }

    // ===== White spell cast trigger =====

    @Test
    @DisplayName("Casting a white spell puts a charge counter on Shrine")
    void whiteSpellAddsChargeCounter() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve charge counter trigger
        harness.passBothPriorities(); // resolve creature spell

        assertThat(getShrine().getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-white spell does not add a charge counter")
    void nonWhiteSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(getShrine().getChargeCounters()).isEqualTo(0);
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

        assertThat(getShrine().getChargeCounters()).isEqualTo(0);
    }

    // ===== Activated ability: token creation =====

    @Test
    @DisplayName("Sacrificing with charge counters creates 1/1 Myr tokens")
    void sacrificeCreatesTokens() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());
        getShrine().setChargeCounters(3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, getShrineIndex(), null, null);
        harness.passBothPriorities(); // resolve activated ability

        // Shrine is sacrificed (no longer on battlefield)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Shrine of Loyal Legions"));

        // 3 Myr tokens created
        long myrCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr")
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1
                        && p.getCard().getAdditionalTypes().contains(CardType.ARTIFACT))
                .count();
        assertThat(myrCount).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificing with 5 charge counters creates 5 tokens")
    void sacrificeWithFiveCountersCreatesFiveTokens() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());
        getShrine().setChargeCounters(5);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, getShrineIndex(), null, null);
        harness.passBothPriorities();

        long myrCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr"))
                .count();
        assertThat(myrCount).isEqualTo(5);
    }

    @Test
    @DisplayName("Sacrificing with 0 charge counters creates no tokens")
    void sacrificeWithZeroCountersCreatesNoTokens() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());
        getShrine().setChargeCounters(0);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, getShrineIndex(), null, null);
        harness.passBothPriorities();

        // Shrine is sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Shrine of Loyal Legions"));

        // No tokens
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Myr"));
    }

    // ===== Combined: upkeep + spell + sacrifice =====

    @Test
    @DisplayName("Accumulate counters via upkeep and white spells, then sacrifice for tokens")
    void fullLifecycle() {
        harness.addToBattlefield(player1, new ShrineOfLoyalLegions());

        // Upkeep: +1 counter
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger
        assertThat(getShrine().getChargeCounters()).isEqualTo(1);

        // Cast white spell: +1 counter
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve charge counter trigger
        harness.passBothPriorities(); // resolve creature spell
        assertThat(getShrine().getChargeCounters()).isEqualTo(2);

        // Sacrifice shrine with 2 counters
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, getShrineIndex(), null, null);
        harness.passBothPriorities();

        long myrCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr"))
                .count();
        assertThat(myrCount).isEqualTo(2);
    }
}
