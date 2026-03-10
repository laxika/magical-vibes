package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BlackcleaveGoblin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HandOfThePraetorsTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has static boost for own infect creatures and spell cast trigger")
    void hasCorrectEffects() {
        HandOfThePraetors card = new HandOfThePraetors();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);
        StaticBoostEffect boost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
        assertThat(boost.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(boost.filter()).isInstanceOf(PermanentHasKeywordPredicate.class);
        PermanentHasKeywordPredicate filter = (PermanentHasKeywordPredicate) boost.filter();
        assertThat(filter.keyword()).isEqualTo(Keyword.INFECT);

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(GiveTargetPlayerPoisonCountersEffect.class);
        GiveTargetPlayerPoisonCountersEffect trigger = (GiveTargetPlayerPoisonCountersEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.amount()).isEqualTo(1);
        assertThat(trigger.spellFilter()).isNotNull();
    }

    // ===== Static boost: other creatures with infect get +1/+1 =====

    @Test
    @DisplayName("Own creature with infect gets +1/+1")
    void ownInfectCreatureGetsBoosted() {
        harness.addToBattlefield(player1, new HandOfThePraetors());
        harness.addToBattlefield(player1, new BlackcleaveGoblin());

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blackcleave Goblin"))
                .findFirst().orElseThrow();

        // Blackcleave Goblin is 2/1; with +1/+1 boost = 3/2
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost itself")
    void doesNotBoostItself() {
        harness.addToBattlefield(player1, new HandOfThePraetors());

        Permanent hand = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hand of the Praetors"))
                .findFirst().orElseThrow();

        // Hand of the Praetors is 3/2 base, should NOT get boosted by its own effect
        assertThat(gqs.getEffectivePower(gd, hand)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hand)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost creature without infect")
    void doesNotBoostNonInfectCreature() {
        harness.addToBattlefield(player1, new HandOfThePraetors());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Grizzly Bears is 2/2, no infect, should not be boosted
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost opponent's infect creature")
    void doesNotBoostOpponentInfectCreature() {
        harness.addToBattlefield(player1, new HandOfThePraetors());
        harness.addToBattlefield(player2, new BlackcleaveGoblin());

        Permanent goblin = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blackcleave Goblin"))
                .findFirst().orElseThrow();

        // Opponent's Blackcleave Goblin should remain 2/1
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost is lost when Hand of the Praetors leaves the battlefield")
    void boostLostWhenLordRemoved() {
        harness.addToBattlefield(player1, new HandOfThePraetors());
        harness.addToBattlefield(player1, new BlackcleaveGoblin());

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blackcleave Goblin"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);

        // Remove the lord
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Hand of the Praetors"));

        // Goblin should revert to 2/1
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
    }

    // ===== Triggered ability: casting infect creature gives opponent a poison counter =====

    @Test
    @DisplayName("Casting a creature with infect triggers poison counter on chosen player")
    void castingInfectCreatureTriggersPoisonCounter() {
        harness.addToBattlefield(player1, new HandOfThePraetors());
        harness.setHand(player1, List.of(new BlackcleaveGoblin()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        int poisonBefore = gd.playerPoisonCounters.getOrDefault(player2.getId(), 0);

        harness.castCreature(player1, 0);

        // Player must choose a target player for the poison trigger
        harness.handlePermanentChosen(player1, player2.getId());

        // Triggered ability should now be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Hand of the Praetors"));

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0))
                .isEqualTo(poisonBefore + 1);
    }

    @Test
    @DisplayName("Casting a creature without infect does not trigger")
    void castingNonInfectCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new HandOfThePraetors());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int poisonBefore = gd.playerPoisonCounters.getOrDefault(player2.getId(), 0);

        harness.castCreature(player1, 0);

        // No triggered ability on the stack (only the creature spell)
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Hand of the Praetors"));

        // Resolve the creature spell
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0))
                .isEqualTo(poisonBefore);
    }

    @Test
    @DisplayName("Opponent casting infect creature does not trigger Hand of the Praetors")
    void opponentCastingInfectCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new HandOfThePraetors());

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new BlackcleaveGoblin()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castCreature(player2, 0);

        // No triggered ability from Hand of the Praetors
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Hand of the Praetors"));
    }

    // ===== Multiple infect creature casts accumulate poison =====

    @Test
    @DisplayName("Multiple infect creature casts accumulate poison counters")
    void multipleInfectCastsAccumulatePoison() {
        harness.addToBattlefield(player1, new HandOfThePraetors());
        harness.setHand(player1, List.of(new BlackcleaveGoblin()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        // Choose target player for poison trigger
        harness.handlePermanentChosen(player1, player2.getId());
        // Resolve triggered ability
        harness.passBothPriorities();
        // Resolve creature spell
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);

        // Cast another infect creature
        harness.setHand(player1, List.of(new BlackcleaveGoblin()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target self with poison counter trigger")
    void canTargetSelfWithPoisonTrigger() {
        harness.addToBattlefield(player1, new HandOfThePraetors());
        harness.setHand(player1, List.of(new BlackcleaveGoblin()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        int poisonBefore = gd.playerPoisonCounters.getOrDefault(player1.getId(), 0);

        harness.castCreature(player1, 0);

        // Choose self as target for poison counter
        harness.handlePermanentChosen(player1, player1.getId());

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0))
                .isEqualTo(poisonBefore + 1);
        // Opponent should not have gotten a poison counter
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
